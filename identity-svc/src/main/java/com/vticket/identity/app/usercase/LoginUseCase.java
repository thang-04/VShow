package com.vticket.identity.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.identity.app.dto.req.LoginRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jwt.JwtService;
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
        String prefix = "[LoginUseCase]|request=" + gson.toJson(request);
        log.info(prefix);
        try {
            Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
            if (!userOptional.isPresent()) {
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

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                user.updateTokens(accessToken, refreshToken);
                userRepository.save(user);

                TokenResponse tokens = TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
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
        } catch (Exception e) {
            log.error("{}|Exception={}", prefix, e.getMessage());
            return null;
        }
    }
}

