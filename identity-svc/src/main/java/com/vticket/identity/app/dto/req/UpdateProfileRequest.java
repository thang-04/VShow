package com.vticket.identity.app.dto.req;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class UpdateProfileRequest {
    @Email(message = "Email is not valid")
    private String email;
    private String address;
    private String avatar;
    private String phone;
}
