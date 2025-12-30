package com.vticket.identity.app.dto.res;

import com.vticket.identity.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private String deviceId;
    private String phone;
    private String address;
    private Set<Role> roles;
    private boolean active;
}
