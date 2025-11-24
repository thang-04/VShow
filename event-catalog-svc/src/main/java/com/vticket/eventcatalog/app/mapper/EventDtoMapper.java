package com.vticket.eventcatalog.app.mapper;

import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.domain.entity.Event;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventDtoMapper {

    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    List<Event> toEntityList(List<Event> list);
}

