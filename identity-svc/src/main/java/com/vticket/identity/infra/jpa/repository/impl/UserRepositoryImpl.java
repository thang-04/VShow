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
        if (saved == null) {
            log.error("Save user failed");
            return null;
        }
        log.info("Save success user={}", gson.toJson(saved));
        return userEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<User> updateProfile(String uid, UpdateProfileRequest user) {
        String prefix = "[updateProfile]|user_id=" + uid;
        log.info("{}|req={}", prefix, gson.toJson(user));
        int result = jpaRepository.updateProfile(uid, user.getAvatar(), user.getEmail(), user.getAddress());
        if (result == 0) {
            log.error("{}|Update profile failed", prefix);
            return Optional.empty();
        }
        log.info("{}|updateProfile success", prefix);
        return findById(uid);
    }

    @Override
    public Optional<User> findById(String id) {
        String prefix = "[findById]|user_id=" + id;
        Optional<User> user = jpaRepository.findById(id)
                .map(userEntityMapper::toDomain);
        if (user.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        }
        log.info("{}|SUCCESS|Find by user_id={}", prefix, id);
        return user;
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(userEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(userEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByDeviceId(String deviceId) {
        return jpaRepository.findByDeviceId(deviceId)
                .map(userEntityMapper::toDomain);
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
        jpaRepository.deleteById(id);
    }

}

