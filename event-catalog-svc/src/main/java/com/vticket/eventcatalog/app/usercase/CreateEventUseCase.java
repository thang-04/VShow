package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.app.dto.req.CreateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;

    public EventResponse execute(CreateEventRequest request) {
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
        return eventDtoMapper.toResponse(saved);
    }
}

