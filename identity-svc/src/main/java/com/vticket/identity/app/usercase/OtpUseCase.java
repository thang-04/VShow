package com.vticket.identity.app.usercase;

import com.vticket.identity.app.dto.req.OtpVerifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpUseCase {
    private static final SecureRandom secureRandom = new SecureRandom();

    public boolean verifyOtp(OtpVerifyRequest otpVerifyRequest){
        long start = System.currentTimeMillis();
        // Implement OTP verification logic here
        log.info("OTP verification attempted for phone {} with time {}", otpVerifyRequest.getPhone(), (System.currentTimeMillis() - start));
        return true;
    }

    public boolean resendRegistrationOtp(String phone){
        long start = System.currentTimeMillis();
        // Implement OTP resend logic here
        log.info("Resent OTP for phone {} with time {}", phone, (System.currentTimeMillis() - start));
        return true;
    }

}
