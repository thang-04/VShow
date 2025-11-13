package com.vticket.identity.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectResponse {
    private boolean valid;
    private String userId;
    private String username;
    private String email;
    private Instant issuedAt;
    private Instant expiresAt;
}

