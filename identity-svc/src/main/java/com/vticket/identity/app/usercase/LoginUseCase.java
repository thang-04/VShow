package com.vticket.identity.app.usercase;

import com.vticket.identity.app.dto.req.LoginRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse execute(LoginRequest request) {
        String prefix = "[LoginUseCase]|username=" + request.getUsername();
        log.info(prefix);
        try {
            Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
            if (userOptional.isEmpty()) {
                log.info("{}|User not found", prefix);
                return null;
            } else {
                User user = userOptional.get();
                if (!user.isActive()) {
                    log.info("{}|User={} is not active", prefix, user.getUsername());
                    return null;
                }
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    log.info("{}|User={} Password Mismatch", prefix, user.getUsername());
                    return null;
                }

                if (user.getAccessToken() != null && !user.getAccessToken().isEmpty()) {
                    User userInfo = getUserFromAccessToken(user.getAccessToken());
                    if (userInfo != null) {
                        log.info("{}|Token is still valid for user: {}", prefix, request.getUsername());
                        TokenResponse tokens = TokenResponse.builder()
                                .accessToken(userInfo.getAccessToken())
                                .refreshToken(userInfo.getRefreshToken())
                                .tokenType("Bearer")
                                .expiresIn(ChronoUnit.SECONDS.between(Instant.now(),
                                        Instant.now().plus(60, ChronoUnit.MINUTES)))
                                .build();
                        log.info("{}|Login Successful|user={}", prefix, gson.toJson(user));
                        return LoginResponse.builder()
                                .userId(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .tokens(tokens)
                                .build();
                    } else {
                        log.info("{}|Token expired for user: {}, refreshing token", prefix, request.getUsername());
                        String accessToken = jwtService.generateAccessToken(user);
                        String refreshToken = jwtService.generateRefreshToken(user);
                        String refreshTokenHash = passwordEncoder.encode(refreshToken);
                        user.updateTokens(accessToken, refreshTokenHash);
                        userRepository.save(user);

                        TokenResponse tokens = TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshTokenHash)
                                .tokenType("Bearer")
                                .expiresIn(ChronoUnit.SECONDS.between(Instant.now(),
                                        Instant.now().plus(60, ChronoUnit.MINUTES)))
                                .build();
                        log.info("{}|Login Successful|user={}", prefix, gson.toJson(user));
                        return LoginResponse.builder()
                                .userId(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .tokens(tokens)
                                .build();
                    }
                } else {
                    log.info("{}|No existing token for user: {}, creating new token", prefix, request.getUsername());
                    String accessToken = jwtService.generateAccessToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    String refreshTokenHash = passwordEncoder.encode(refreshToken);
                    user.updateTokens(accessToken, refreshTokenHash);
                    userRepository.save(user);

                    TokenResponse tokens = TokenResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshTokenHash)
                            .tokenType("Bearer")
                            .expiresIn(ChronoUnit.SECONDS.between(Instant.now(),
                                    Instant.now().plus(60, ChronoUnit.MINUTES)))
                            .build();
                    log.info("{}|Login Successful|user={}", prefix, gson.toJson(user));
                    return LoginResponse.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .tokens(tokens)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
            return null;
        }
    }

    public User getUserFromAccessToken(String token) {
        log.info("Retrieving user from access token");
        if (StringUtils.isBlank(token)) {
            log.error("Access token is blank or null");
        }
        User user;
        try {
            user = jwtService.verifyFromAccessToken(token);
            if (user != null && user.getId() != null) {
                user = userRepository.findById(user.getId()).get();
                if (!token.equals(user.getAccessToken())) {
                    log.error("Token mismatch for user ID: {}", user.getId());
                } else {
                    log.info("Successfully retrieved user from access token for user ID: {}", user.getId());
                    return user;
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving user with access token: {}", e.getMessage(), e);
        }
        return null;
    }
}

