package com.vticket.identity.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank(message = "Email is required")
    private String email;
}

