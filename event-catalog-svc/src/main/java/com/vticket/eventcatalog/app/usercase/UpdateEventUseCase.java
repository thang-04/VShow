package com.vticket.eventcatalog.app.usercase;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.eventcatalog.app.dto.req.UpdateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;

    public EventResponse execute(Long eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getPrice() != null) event.setPrice(request.getPrice());
        if (request.getVenue() != null) event.setVenue(request.getVenue());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getCategoryId() != null) event.setCategoryId(request.getCategoryId());

        event.setUpdatedAt(LocalDateTime.now());
        Event updated = eventRepository.save(event);
        return eventDtoMapper.toResponse(updated);
    }
}

