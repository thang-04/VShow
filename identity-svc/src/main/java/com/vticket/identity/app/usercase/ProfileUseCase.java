package com.vticket.identity.app.usercase;

import com.google.gson.reflect.TypeToken;
import com.vticket.commonlibs.utils.CommonUtils;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.identity.app.dto.req.UpdateProfileRequest;
import com.vticket.identity.app.dto.res.UserResponse;
import com.vticket.identity.app.mapper.IdentityDtoMapper;
import com.vticket.identity.domain.entity.User;
import com.vticket.identity.domain.repository.UserRepository;
import com.vticket.identity.infra.redis.RedisService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileUseCase {
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final IdentityDtoMapper identityDtoMapper;

    public void putUserInfoToRedis(User user) {
        String keyRedis = Constant.RedisKey.USER_ID + user.getId();
        //add user to redis
        redisService.getRedisStringTemplate().opsForValue().set(keyRedis, gson.toJson(user));
        //set time expire 30p
        redisService.getRedisStringTemplate().expire(keyRedis, 30L, TimeUnit.MINUTES);
    }

    public List<UserResponse> getAllUser() {
        String prefix = "[getAllUser]";
        log.info("{}|Retrieving all users", prefix);
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.error("{}|No users found in database", prefix);
            return null;
        }
        log.info("{}|Successfully retrieved {} users", prefix, users.size());
        return identityDtoMapper.toResponseList(users);
    }

    public User getUserByUserName(String username) {
        String prefix = "[getUserByUserName]";
        log.info("{}|Retrieving user by username: {}", prefix, username);
        if (StringUtils.isBlank(username)) {
            log.error("{}|Username is blank or null", prefix);
            return null;
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.error("{}|User not found with username: {}", prefix, username);
            return null;
        }
        log.info("{}|Successfully retrieved user: {}", prefix, username);
        return user.get();
    }

    public User getUserById(String id) {
        String prefix = "[getUserById]";
        log.info("{}|Retrieving user by ID: {}", prefix, id);
        long start = System.currentTimeMillis();
        String dataFrom = "BY REDIS";
        String keyRedis = Constant.RedisKey.USER_ID + id;
        String resultRedis;
        Optional<User> user = Optional.empty();
        try {
            // check in redis
            resultRedis = redisService.getRedisStringTemplate().opsForValue().get(keyRedis);
            if (!StringUtils.isBlank(resultRedis)) {
                user = gson.fromJson(resultRedis, new TypeToken<User>() {
                }.getType());
                log.info("{}|User found in Redis cache  : {} with time: {}ms", prefix, user, (System.currentTimeMillis() - start));
            } else {
                // get in db
                user = userRepository.findById(id);
                dataFrom = "BY MYSQL";
                log.info("{}|User retrieved from MYSQL  : {} with time: {}ms", prefix, user, (System.currentTimeMillis() - start));
                if (user.isEmpty()) {
                    log.info("{}|User not found with ID: {}", prefix, id);
                } else {
                    putUserInfoToRedis(user.get());
                }
            }
        } catch (Exception e) {
            log.error("{}|Error accessing Redis for user ID: {} - {}", prefix, id, e.getMessage(), e);
        }
        log.info("{}|Successfully|retrieved={}|user={}", prefix, dataFrom, id);
        if (user.isEmpty()) {
            log.error("{}|User not found with ID: {}", prefix, id);
            return null;
        }
        return user.get();
    }

    public UserResponse getUserByUId(String id) {
        String prefix = "[getUserByUId]|Uid=" + id;
        log.info("{}|Retrieving user", prefix);
        long start = System.currentTimeMillis();
        String dataFrom = "BY REDIS";
        String keyRedis = Constant.RedisKey.USER_ID + id;
        String resultRedis;
        Optional<User> user = Optional.empty();
        try {
            // get redis
            resultRedis = redisService.getRedisStringTemplate().opsForValue().get(keyRedis);
            if (!StringUtils.isBlank(resultRedis)) {
                User userFromRedis = gson.fromJson(resultRedis, User.class);
                user = Optional.ofNullable(userFromRedis);
                log.info("{}|User found in Redis cache for : {} with time: {}ms", prefix, gson.toJson(userFromRedis), (System.currentTimeMillis() - start));
            } else {
                // get db
                user = userRepository.findById(id);
                dataFrom = "BY MYSQL";
                log.info("{}|User retrieved from MYSQL for : {} with time: {}ms", prefix, user, (System.currentTimeMillis() - start));
                if (user.isEmpty()) {
                    log.info("{}|User not found with ID: {}", prefix, id);
                } else {
                    putUserInfoToRedis(user.get());
                }
            }
        } catch (Exception e) {
            log.error("{}|Error accessing Redis for user ID: {} - {}", prefix, id, e.getMessage(), e);
        }
        log.info("{}|Successfully retrieved={} ", prefix, dataFrom);
        if (user.isEmpty()) {
            log.error("{}|User not found with ID: {}", prefix, id);
            return null;
        }
        return identityDtoMapper.toResponse(user.get());
    }

    public UserResponse updateUserByUId(String id, UpdateProfileRequest updateRequest) {
        String prefix = "[updateUserByUId]|Uid=" + id + "|data=" + gson.toJson(updateRequest);
        log.info("{}|Retrieving user", prefix);
        long start = System.currentTimeMillis();
        String keyRedis = Constant.RedisKey.USER_ID + id;
        try {
            UserResponse profile = getUserByUId(id);
            if (profile == null) {
                log.error("{}|User not found with ID: {}", prefix, id);
                return null;
            }
            String email = updateRequest.getEmail();
            if (StringUtils.isNotEmpty(email) && !CommonUtils.isEmail(email)) {
                log.error("{}|Email invalid|userId={}", prefix, id);
                return null;
            }
            Optional<User> result = userRepository.updateProfile(id, updateRequest);
            if (result.isEmpty()) {
                log.error("{}|FAILED_UPDATE", prefix);
                return null;
            }
            log.info("{}|SUCCESS_UPDATE|With time: {}ms", prefix, (System.currentTimeMillis() - start));
            //refresh cache - set new cache instead of just deleting
            redisService.getRedisStringTemplate.delete(keyRedis);
            putUserInfoToRedis(result.get());
            return identityDtoMapper.toResponse(result.get());
        } catch (Exception e) {
            log.error("{}|Error accessing Redis for user ID: {} - {}", prefix, id, e.getMessage(), e);
            return null;
        }
    }
}
