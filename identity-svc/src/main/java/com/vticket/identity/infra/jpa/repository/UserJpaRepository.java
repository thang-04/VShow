package com.vticket.identity.infra.jpa.repository;

import com.vticket.identity.infra.jpa.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByDeviceId(String deviceId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

