package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.Constant;
import com.vticket.eventcatalog.app.dto.req.UpdateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final RedisService redisService;

    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {
        String prefix = "[UpdateEventUseCase]|";
        long start = System.currentTimeMillis();
        try {
            String key = String.format(Constant.RedisKey.REDIS_EVENT_BY_ID, eventId);
            String resultRedis = redisService.getRedisEventTemplate().opsForValue().get(key);
            if (resultRedis != null) {
                // remove cache redis
                redisService.getRedisEventTemplate().delete(key);
                log.info("{}|Deleted event cache in Redis for id {}.", prefix, eventId);
            }
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            if (request.getTitle() != null)
                event.setTitle(request.getTitle());
            if (request.getDescription() != null)
                event.setDescription(request.getDescription());
            if (request.getPrice() != null)
                event.setPrice(request.getPrice());
            if (request.getVenue() != null)
                event.setVenue(request.getVenue());
            if (request.getStartTime() != null)
                event.setStartTime(request.getStartTime());
            if (request.getEndTime() != null)
                event.setEndTime(request.getEndTime());
            if (request.getCategoryId() != null)
                event.setCategoryId(request.getCategoryId());

            event.setUpdatedAt(new Date());
            Event updated = eventRepository.save(event);
            if (updated == null) {
                log.error("{}|Failed to update event in MySQL for id {}.", prefix, eventId);
                return null;
            }
            log.info("{}|Updated event in MySQL for id {}. Time taken: {} ms", prefix, eventId,
                    (System.currentTimeMillis() - start));
            return eventDtoMapper.toResponse(updated);
        } catch (Exception e) {
            log.error("{}|Exception|{}", prefix, e.getMessage(), e);
            return null;
        }
    }
}
