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

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final OtpUseCase otpUseCase;
    private final KeyRegistry keyRegistry;
    private final RSAService rSAService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        String prefix = "[AuthController]|register|";
        try {
            log.info("{}|Register request for username: {}",prefix, request.getUsername());
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

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody OtpVerifyRequest request){
        String prefix = "[AuthController]|verifyOtp|";
        log.info("{}|OTP verification request for email: {}",prefix, gson.toJson(request));
        try{
            boolean isVerify = otpUseCase.verifyOtp(request);
          if(!isVerify){
              return ResponseJson.of(ErrorCode.INVALID_OTP, "Invalid OTP");
          }
        }catch (Exception e){
            log.error("OTP verification failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_OTP, e.getMessage());
        }
        return ResponseJson.success("OTP verified successfully", null);
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestBody OtpVerifyRequest request){
        String prefix = "[AuthController]|resendOtp|";
        try{
            boolean isResend = otpUseCase.resendRegistrationOtp(request.getPhone());
            if(!isResend){
                return ResponseJson.of(ErrorCode.INVALID_REQUEST, "Failed to resend OTP");
            }
        }catch (Exception e){
            log.error("Resend OTP failed: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
        log.info("{}|Resend OTP request for email: {}",prefix, gson.toJson(request));
        return ResponseJson.success("OTP resent successfully", null);

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

