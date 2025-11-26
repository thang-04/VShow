package com.vticket.identity.infra.jpa.mapper;

import com.vticket.identity.domain.entity.User;
import com.vticket.identity.infra.jpa.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity toEntity(User user);
    User toDomain(UserEntity userEntity);
}
