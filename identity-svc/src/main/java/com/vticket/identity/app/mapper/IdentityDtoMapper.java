package com.vticket.identity.app.mapper;

import com.vticket.identity.app.dto.res.UserResponse;
import com.vticket.identity.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IdentityDtoMapper {
    List<UserResponse> toResponseList(List<User> users);
    UserResponse toResponse(User user);
}
