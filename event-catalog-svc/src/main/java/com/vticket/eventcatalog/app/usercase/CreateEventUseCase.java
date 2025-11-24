package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.req.CreateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final RedisService redisService;

    public EventResponse execute(CreateEventRequest request) {
        String prefix = "[CreateEventUseCase]|";
        long start = System.currentTimeMillis();
        try{
            String key = Constant.RedisKey.REDIS_LIST_EVENT;
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if(resultRedis != null){
                // remove cache redis
                redisService.getRedisEventTemplate().delete(key);
                log.info("{}|Deleted event list cache in Redis.", prefix);
            }
            Event event = Event.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .venue(request.getVenue())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .categoryId(request.getCategoryId())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Event saved = eventRepository.save(event);
            if(saved == null){
                log.error("{}|Failed to create event in MySQL.", prefix);
                return null;
            }
            log.info("{}|Created event success. Time taken: {} ms", prefix, (System.currentTimeMillis() - start));
            return eventDtoMapper.toResponse(saved);
        }catch (Exception ex){
           log.error("{}|Exception|{}",prefix, ex.getMessage(), ex);
           return null;
        }

    }
}

