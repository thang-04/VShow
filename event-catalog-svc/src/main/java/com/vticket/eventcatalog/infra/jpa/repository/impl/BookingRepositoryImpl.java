package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Booking;
import com.vticket.eventcatalog.domain.repository.BookingRepository;
import com.vticket.eventcatalog.infra.jpa.BookingEntity;
import com.vticket.eventcatalog.infra.jpa.mapper.BookingEntityMapper;
import com.vticket.eventcatalog.infra.jpa.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingJpaRepository jpaRepository;
    private final BookingEntityMapper bookingEntityMapper;

    @Override
    public Booking save(Booking booking) {
        String prefix = "[save]|booking_code=" + booking.getBookingCode();
        log.info("{}|Saving booking", prefix);
        BookingEntity saved = jpaRepository.save(bookingEntityMapper.toEntity(booking));
        Booking result = bookingEntityMapper.toDomain(saved);
        log.info("{}|Booking saved successfully|booking_id={}", prefix, result.getId());
        return result;
    }

    @Override
    public Optional<Booking> findByBookingCode(String bookingCode) {
        String prefix = "[findByBookingCode]|booking_code=" + bookingCode;
        Optional<Booking> booking = jpaRepository.findByBookingCode(bookingCode)
                .map(bookingEntityMapper::toDomain);
        if (booking.isEmpty()) {
            log.warn("{}|No booking found", prefix);
        } else {
            log.info("{}|Booking found", prefix);
        }
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        String prefix = "[findById]|booking_id=" + id;
        Optional<Booking> booking = jpaRepository.findById(id)
                .map(bookingEntityMapper::toDomain);
        if (booking.isEmpty()) {
            log.warn("{}|No booking found", prefix);
        } else {
            log.info("{}|Booking found", prefix);
        }
        return booking;
    }
}
