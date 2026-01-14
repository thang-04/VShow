package com.vticket.eventcatalog.infra.jpa.mapper;

import com.vticket.eventcatalog.domain.entity.Booking;
import com.vticket.eventcatalog.infra.jpa.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingEntityMapper {
    BookingEntityMapper INSTANCE = Mappers.getMapper(BookingEntityMapper.class);

    Booking toDomain(BookingEntity entity);

    BookingEntity toEntity(Booking domain);
}
