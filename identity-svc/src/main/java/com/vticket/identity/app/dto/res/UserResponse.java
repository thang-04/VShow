package com.vticket.identity.app.dto.res;

import com.vticket.identity.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String password;
    private String deviceId;
    private Set<Role> roles;
    private String accessToken;
    private String refreshToken;
    private Date createdAt;
    private Date updatedAt;
    private boolean active;
}
