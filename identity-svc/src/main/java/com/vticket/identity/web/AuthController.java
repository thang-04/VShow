package com.vticket.identity.web;

import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.identity.app.dto.req.LoginRequest;
import com.vticket.identity.app.dto.req.RefreshTokenRequest;
import com.vticket.identity.app.dto.req.RegisterRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.app.usercase.LoginUseCase;
import com.vticket.identity.app.usercase.RefreshTokenUseCase;
import com.vticket.identity.app.usercase.RegisterUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/identity/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Register request for username: {}", request.getUsername());
            LoginResponse response = registerUseCase.execute(request);
            return ResponseJson.success("Registration successful", response);
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REGISTER, e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request for username: {}", request.getUsername());
            LoginResponse response = loginUseCase.execute(request);
            return ResponseJson.success("Login successful", response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public String refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.info("Refresh token request");
            TokenResponse response = refreshTokenUseCase.execute(request);
            return ResponseJson.success("Token refreshed successfully", response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REFRESH_TOKEN, e.getMessage());
        }
    }
}

