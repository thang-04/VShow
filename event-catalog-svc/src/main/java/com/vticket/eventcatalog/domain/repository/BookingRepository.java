package com.vticket.eventcatalog.domain.repository;

import com.vticket.eventcatalog.domain.entity.Booking;

import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findById(Long id);
}
