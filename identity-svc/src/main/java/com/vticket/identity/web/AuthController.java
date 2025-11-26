package com.vticket.identity.web;

import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.identity.app.dto.req.LoginRequest;
import com.vticket.identity.app.dto.req.OtpVerifyRequest;
import com.vticket.identity.app.dto.req.RefreshTokenRequest;
import com.vticket.identity.app.dto.req.RegisterRequest;
import com.vticket.identity.app.dto.res.LoginResponse;
import com.vticket.identity.app.dto.res.TokenResponse;
import com.vticket.identity.app.usercase.LoginUseCase;
import com.vticket.identity.app.usercase.OtpUseCase;
import com.vticket.identity.app.usercase.RefreshTokenUseCase;
import com.vticket.identity.app.usercase.RegisterUseCase;
import com.vticket.identity.infra.config.KeyRegistry;
import com.vticket.identity.infra.jwt.RSAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@RestController
@RequestMapping("/api/identity/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String LOG_PREFIX = "[AuthController]";
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final OtpUseCase otpUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final KeyRegistry keyRegistry;
    private final RSAService rSAService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        String prefix = LOG_PREFIX + "|register";
        try {
            log.info("{}|Register request for username: {}", prefix, request.getUsername());
            String response = registerUseCase.executeSendOtp(request);
            if (response == null) {
                log.info("{}|OTP send for user: {} failed ", prefix,request.getEmail());
                return ResponseJson.of(ErrorCode.UNSUCCESS, "Send OTP failed or has already been sent");
            }
            // OTP sent, waiting for verification before inserting user
            log.info("{}|OTP sent for user: {} registration success", prefix, request.getEmail());
            return ResponseJson.success("OTP sent. Please verify to complete registration");
        } catch (Exception e) {
            log.error("{}|Registration failed: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REGISTER, e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        String prefix = LOG_PREFIX + "|login";
        try {
            log.info("{}|Login request for username: {}", prefix, request.getUsername());
            LoginResponse response = loginUseCase.execute(request);
            if (response == null) {
                log.error("{}|Login Failed for username: {}", prefix, request.getUsername());
                return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Login Failed for username: " + request.getUsername());
            }
            return ResponseJson.success("Login successful", response);
        } catch (Exception e) {
            log.error("{}|Login failed: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public String refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String prefix = LOG_PREFIX + "|refresh";
        log.info("{}|Refresh token request", prefix);
        try {
            TokenResponse response = refreshTokenUseCase.execute(request);
            if (response == null) {
                log.info("{}|Refresh token failed", prefix);
                return ResponseJson.of(ErrorCode.EXPIRED_TOKEN, "Refresh token expired");
            }
            log.info("{}|Refresh token successful", prefix);
            return ResponseJson.success("Token refreshed successfully", response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REFRESH_TOKEN, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody OtpVerifyRequest request) {
        String prefix = LOG_PREFIX + "|verifyOtp";
        log.info("{}|OTP verification request for email: {}", prefix, request.getEmail());
        try {
            LoginResponse response = registerUseCase.executeInsert(request);
            if (response == null) {
                log.info("{}|OTP verification failed for email: {}", prefix, request.getEmail());
                return ResponseJson.of(ErrorCode.INVALID_OTP, "Invalid OTP");
            }
            log.info("{}|OTP verification successful for email: {}", prefix, request.getEmail());
            return ResponseJson.success("OTP verified successfully, register user success", response);
        } catch (Exception e) {
            log.error("OTP verification failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_OTP, e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestBody OtpVerifyRequest request) {
        String prefix = LOG_PREFIX + "|resendOtp";
        log.info("{}|OTP resend for email: {}", prefix, request.getEmail());
        try {
            boolean isResend = otpUseCase.resendRegistrationOtp(request);
            if (!isResend) {
                log.info("{}|OTP resend failed for email: {}", prefix, request.getEmail());
                return ResponseJson.of(ErrorCode.UNSUCCESS, "Failed to resend OTP");
            }
            log.info("{}|Resend OTP request for email: {}", prefix, gson.toJson(request));
            return ResponseJson.success("OTP resent successfully", null);
        } catch (Exception e) {
            log.error("Resend OTP failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
    }

    //check keys public valid
    @GetMapping("/jwks")
    public Map<String, Object> jwks() {
        List<Map<String, Object>> keys = new ArrayList<>();
        for (var kp : keyRegistry.getKeys()) {
            //get public key
            RSAPublicKey pub = rSAService.loadPublicKey(kp.getPublicPem());
            //get modulus(n) and exponent(e) from public key
            String n = base64Url(pub.getModulus());
            String e = base64Url(pub.getPublicExponent());
            keys.add(Map.of(
                    "kty", "RSA",
                    "kid", kp.getKid(),
                    "alg", "RS256",
                    "use", "sig",
                    "n", n,
                    "e", e
            ));
        }
        return Map.of("keys", keys);
    }

    private static String base64Url(BigInteger bi) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bi.toByteArray())
                .replaceFirst("^AA+", "");
    }
}

