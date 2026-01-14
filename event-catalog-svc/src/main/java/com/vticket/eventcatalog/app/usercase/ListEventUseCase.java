package com.vticket.eventcatalog.app.usercase;

import com.google.gson.reflect.TypeToken;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.commonlibs.utils.CommonUtils.gson;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListEventUseCase {

    private static final long EVENT_TTL_HOURS = 1L;
    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final RedisService redisService;

    public List<EventResponse> execute() {
        String prefix = "[ListEventUseCase]|";
        long start = System.currentTimeMillis();
        List<Event> listEvent = new ArrayList<>();
        try {
            String key = Constant.RedisKey.REDIS_LIST_EVENT;
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if (resultRedis == null || resultRedis.isEmpty()) {
                //get by MYSQL
                List<Event> list = eventRepository.findActiveEvents();
                log.info("{}|Fetched events from MySQL: {} events found.", prefix, list.size());
                if (!list.isEmpty()) {
                    List<Event> mappedEvents = eventDtoMapper.toEntityList(list);
                    listEvent.addAll(mappedEvents);
                }
                //cache redis
                if (!listEvent.isEmpty()) {
                    redisService.getRedisEventTemplate().opsForValue().set(key, gson.toJson(listEvent));
                    redisService.getRedisEventTemplate().expire(key, EVENT_TTL_HOURS, TimeUnit.HOURS);
                    log.info("{}|Stored events in Redis cache.", prefix);
                }
            } else {
                listEvent = (List<Event>) gson.fromJson(resultRedis, new TypeToken<List<Event>>() {
                }.getType());
                log.info("{}|Fetched events from Redis cache: {} events found.", prefix, listEvent.size());
            }
            log.info("{}|getListSeat in Redis|Time taken: {} ms", prefix, (System.currentTimeMillis() - start));
            return eventDtoMapper.toResponseList(listEvent);
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
            return List.of();
        }
    }

    public List<EventResponse> executeByCategory(Long categoryId) {
        String prefix = "[executeByCategoryID]|";
        long start = System.currentTimeMillis();
        log.info("{}|Listing events for category ID: {} | Start time: {} ms", prefix, categoryId, start);
        List<Event> listEvents = new ArrayList<>();
        try {
            String key = String.format(Constant.RedisKey.REDIS_EVENT_BY_CATEGORY_ID, categoryId);
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if (StringUtils.isEmpty(resultRedis)) {
                //get by MYSQL
                List<Event> list = eventRepository.findByCategoryId(categoryId);
                log.info("{}|Fetched events from MySQL for category {}: {} events found.", prefix, categoryId, list.size());
                if (!list.isEmpty()) {
                    List<Event> mappedEvents = eventDtoMapper.toEntityList(list);
                    listEvents.addAll(mappedEvents);
                }
                //cache redis
                if (!listEvents.isEmpty()) {
                    redisService.getRedisEventTemplate().opsForValue().set(key, gson.toJson(listEvents));
                    redisService.getRedisEventTemplate().expire(key, EVENT_TTL_HOURS, TimeUnit.HOURS);
                    log.info("{}|Stored events in Redis cache for category ID: {}.", prefix, categoryId);
                }
            } else {
                List<Event> cachedEvents = (List<Event>) gson.fromJson(resultRedis, new TypeToken<List<Event>>() {
                }.getType());
                log.info("{}|Fetched events from Redis cache for category {}: {} events found.", prefix, categoryId, cachedEvents.size());
            }
            log.info("{}|getListSeat in Redis by Category|Time taken: {} ms", prefix, (System.currentTimeMillis() - start));
            return eventDtoMapper.toResponseList(listEvents);
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
            return List.of();
        }
    }
}

