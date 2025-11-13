package com.vticket.eventcatalog.infra.jpa.mapper;

import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.infra.jpa.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventEntityMapper {

    EventEntity toEntity(Event event);

    Event toDomain(EventEntity eventEntity);

    List<Event> toDomainList(List<EventEntity> entities);
}

