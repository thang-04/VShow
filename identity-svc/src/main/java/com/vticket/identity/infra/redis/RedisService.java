package com.vticket.identity.infra.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class RedisService {
    private final RedisTemplate<String, Object> redisUITemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
}
