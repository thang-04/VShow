package com.vticket.identity.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("USER", "Regular user"),
    ADMIN("ADMIN", "Administrator");

    private final String code;
    private final String description;
}

