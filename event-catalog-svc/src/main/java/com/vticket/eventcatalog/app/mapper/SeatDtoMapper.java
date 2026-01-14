package com.vticket.eventcatalog.app.mapper;

import com.vticket.eventcatalog.app.dto.res.SeatResponse;
import com.vticket.eventcatalog.domain.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatDtoMapper {
    SeatDtoMapper INSTANCE = Mappers.getMapper(SeatDtoMapper.class);

    @Mapping(target = "ticketTypeId", source = "ticketTypeId")
    SeatResponse toResponse(Seat seat);

    List<SeatResponse> toResponseList(List<Seat> seats);
}
