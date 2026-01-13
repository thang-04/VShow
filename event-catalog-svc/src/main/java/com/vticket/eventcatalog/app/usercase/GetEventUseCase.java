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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final RedisService redisService;

    public EventResponse execute(Long eventId) {
        String prefix = "[GetEventUseCase]|eventId=" + eventId;
        String key = String.format(Constant.RedisKey.REDIS_LIST_EVENT,eventId);
        try {

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

            return eventDtoMapper.toResponse(event);
        }catch(Exception e){
            log.error("{}|Error retrieving event from Redis: {}", prefix, e.getMessage());
            return null;
        }
    }
}

