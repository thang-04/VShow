package com.vticket.identity.infra.jpa.repository.impl;

import com.google.gson.Gson;
import com.vticket.identity.app.dto.req.UpdateProfileRequest;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.jpa.UserEntity;
import com.vticket.identity.infra.jpa.mapper.UserEntityMapper;
import com.vticket.identity.infra.jpa.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper userEntityMapper;
    private final Gson gson;

    @Override
    public User save(User user) {
        log.info("Save user={}", gson.toJson(user));
        UserEntity entity = userEntityMapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return userEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<User> updateProfile(String uid, UpdateProfileRequest user) {
        String prefix = "[updateProfile]|user_id=" + uid;
        log.info("{}|req={}", prefix, gson.toJson(user));
        int result = jpaRepository.updateProfile(uid, user.getAvatar(), user.getEmail(), user.getAddress());
        if (result == 0) {
            log.error("{}|Update profile failed|No rows affected", prefix);
            return Optional.empty();
        }
        log.info("{}|Update profile success|Rows affected={}", prefix, result);
        return findById(uid);
    }

    @Override
    public Optional<User> findById(String id) {
        String prefix = "[findById]|user_id=" + id;
        Optional<User> user = jpaRepository.findById(id)
                .map(userEntityMapper::toDomain);
        if (user.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|Find by user_id={}", prefix, id);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(userEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String prefix = "[findByUsername]|username=" + username;
        Optional<User> user = jpaRepository.findByUsername(username)
                .map(userEntityMapper::toDomain);
        if (user.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|User found", prefix);
        }
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String prefix = "[findByEmail]|email=" + email;
        Optional<User> user = jpaRepository.findByEmail(email)
                .map(userEntityMapper::toDomain);
        if (user.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|User found", prefix);
        }
        return user;
    }

    @Override
    public Optional<User> findByDeviceId(String deviceId) {
        String prefix = "[findByDeviceId]|device_id=" + deviceId;
        Optional<User> user = jpaRepository.findByDeviceId(deviceId)
                .map(userEntityMapper::toDomain);
        if (user.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|User found", prefix);
        }
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(String id) {
        String prefix = "[deleteById]|user_id=" + id;
        log.info("{}|Deleting user", prefix);
        jpaRepository.deleteById(id);
        log.info("{}|User deleted successfully", prefix);
    }

}

