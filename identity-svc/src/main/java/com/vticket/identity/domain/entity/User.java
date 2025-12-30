package com.vticket.identity.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Builder
public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String deviceId;
    private String avatar;
    private String phone;
    private String address;
    private Set<Role> roles;
    private String accessToken;
    private String refreshToken;
    private Date createdAt;
    private Date updatedAt;
    private boolean active;

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.updatedAt = new Date();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        this.updatedAt = new Date();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.updatedAt = new Date();
    }
}

