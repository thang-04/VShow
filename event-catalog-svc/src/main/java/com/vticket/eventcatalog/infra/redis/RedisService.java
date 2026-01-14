package com.vticket.eventcatalog.infra.redis;

import com.vticket.commonlibs.utils.Constant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
@Setter
public class RedisService {
    private final RedisTemplate<String, Object> redisUITemplate;
    private final RedisTemplate<String, String> redisEventTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    //Seat Status Hash Operations
    public Map<Object, Object> getSeatStatusHash(Long eventId) {
        String key = String.format(Constant.RedisKey.SEAT_STATUS, eventId);
        return redisUITemplate.opsForHash().entries(key);
    }

    public void updateSeatStatus(Long eventId, Map<String, String> statusMap) {
        String key = String.format(Constant.RedisKey.SEAT_STATUS, eventId);
        redisUITemplate.opsForHash().putAll(key, statusMap);
        redisUITemplate.expire(key, 1, TimeUnit.DAYS);
        log.info("Updated seat status for eventId={}, seats={}", eventId, statusMap.size());
    }

    public void cacheSeatStatus(Long eventId, Map<String, String> statusMap) {
        String key = String.format(Constant.RedisKey.SEAT_STATUS, eventId);
        redisUITemplate.opsForHash().putAll(key, statusMap);
        redisUITemplate.expire(key, 1, TimeUnit.DAYS);
        log.info("Cached seat status for eventId={}, seats={}", eventId, statusMap.size());
    }

    //Seat Hold ZSet Operations
    public boolean holdSeat(Long eventId, Long seatId, long timestamp) {
        String key = Constant.RedisKey.SEAT_HOLD + eventId + "_" + seatId;
        String value = "seat_" + seatId + "_" + timestamp;
        Boolean added = redisUITemplate.opsForZSet().add(key, value, timestamp);
        if (Boolean.TRUE.equals(added)) {
            redisUITemplate.expire(key, 3, TimeUnit.MINUTES);
            log.info("Seat {} held for eventId={}", seatId, eventId);
            return true;
        }
        return false;
    }

    public void releaseSeat(Long eventId, Long seatId) {
        String key = Constant.RedisKey.SEAT_HOLD + eventId + "_" + seatId;
        redisUITemplate.opsForZSet().removeRangeByScore(key, 0, System.currentTimeMillis());

        Long size = redisUITemplate.opsForZSet().zCard(key);
        if (size == null || size == 0) {
            redisUITemplate.delete(key);
        }
        log.info("Released seat {} for eventId={}", seatId, eventId);
    }

    public void releaseSeats(Long eventId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            releaseSeat(eventId, seatId);
        }
    }

    public Set<String> getHoldSeats(Long eventId) {
        return new HashSet<>();
    }

    public boolean checkSeatHold(Long eventId, Long seatId) {
        String key = Constant.RedisKey.SEAT_HOLD + eventId + "_" + seatId;
        Long count = redisUITemplate.opsForZSet().zCard(key);
        return count != null && count > 0;
    }

    public List<Long> getHoldSeatIds(Long eventId, List<Long> seatIds) {
        List<Long> held = new ArrayList<>();
        for (Long seatId : seatIds) {
            if (checkSeatHold(eventId, seatId)) {
                held.add(seatId);
            }
        }
        return held;
    }

    //Atomic ordering for seat hold requests
    public Long incrementOrderKey(Long eventId, List<Long> seatIds) {
        String orderKey = "queue:event_" + eventId + "_hold_order_seatIds[" +
                seatIds.stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> a + "_" + b)
                        .orElse("") + "]";
        Long order = stringRedisTemplate.opsForValue().increment(orderKey);
        stringRedisTemplate.expire(orderKey, 5, TimeUnit.SECONDS);
        return order;
    }

    public void deleteOrderKey(Long eventId, List<Long> seatIds) {
        String orderKey = "queue:event_" + eventId + "_hold_order_seatIds[" +
                seatIds.stream().map(String::valueOf).reduce((a, b) -> a + "_" + b).orElse("") + "]";
        stringRedisTemplate.delete(orderKey);
    }
}
