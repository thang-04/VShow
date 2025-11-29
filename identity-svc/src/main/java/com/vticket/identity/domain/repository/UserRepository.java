package com.vticket.identity.domain.repository;

import com.vticket.identity.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    List<User> findAll();
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByDeviceId(String deviceId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(String id);
}

