package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.mapper.EventDtoMapper;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListEventUseCase {

    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;

    public List<EventResponse> execute() {
        List<Event> events = eventRepository.findActiveEvents();
        return eventDtoMapper.toResponseList(events);
    }

    public List<EventResponse> executeByCategory(Long categoryId) {
        List<Event> events = eventRepository.findByCategoryId(categoryId);
        return eventDtoMapper.toResponseList(events);
    }
}

