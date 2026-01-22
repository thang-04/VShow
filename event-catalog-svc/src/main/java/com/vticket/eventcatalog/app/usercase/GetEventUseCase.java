package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetEventUseCase {

    private static final long EVENT_TTL_HOURS = 1L;
    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final RedisService redisService;

    public EventResponse getEventById(Long eventId) {
        String prefix = "[GetEventById]|";
        long start = System.currentTimeMillis();
        log.info("{}|Get event by id {}|Start|", prefix, eventId);
        Event event = null;
        try {
            String key = String.format(Constant.RedisKey.REDIS_EVENT_BY_ID, eventId);
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                // get by MYSQL
                event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
                log.info("{}|Fetched event from MySQL for id {}.", prefix, eventId);
                if (event != null) {
                    // cache redis
                    redisService.getRedisEventTemplate().opsForValue().set(key, gson.toJson(event));
                    redisService.getRedisEventTemplate().expire(key, EVENT_TTL_HOURS, TimeUnit.HOURS);
                    log.info("{}|Stored event in Redis cache for id {}.", prefix, eventId);
                    return eventDtoMapper.toResponse(event);
                }
            } else {
                event = gson.fromJson(resultRedis, Event.class);
                log.info("{}|Fetched event from Redis cache for id {}.", prefix, eventId);
            }
            log.info("{}|Get event by id {}|Time taken: {} ms", prefix, eventId, (System.currentTimeMillis() - start));
            return eventDtoMapper.toResponse(event);
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
            return null;
        }
    }
}
