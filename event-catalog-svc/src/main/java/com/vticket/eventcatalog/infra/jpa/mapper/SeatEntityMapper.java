package com.vticket.eventcatalog.infra.jpa.mapper;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.infra.jpa.SeatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatEntityMapper {
    SeatEntityMapper INSTANCE = Mappers.getMapper(SeatEntityMapper.class);

    @Mapping(target = "ticketTypeId", source = "ticketTypeId")
    @Mapping(target = "ticketType", ignore = true)
    Seat toDomain(SeatEntity entity);

    @Mapping(target = "ticketTypeId", source = "ticketTypeId")
    @Mapping(target = "ticketType", ignore = true)
    SeatEntity toEntity(Seat domain);

    List<Seat> toDomainList(List<SeatEntity> entities);

    List<SeatEntity> toEntityList(List<Seat> domains);
}
