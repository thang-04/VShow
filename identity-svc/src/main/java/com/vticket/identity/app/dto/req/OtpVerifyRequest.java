package com.vticket.identity.app.dto.req;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OtpVerifyRequest {
    private String phone;
    private String otp;
}
