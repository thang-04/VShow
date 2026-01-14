package com.vticket.eventcatalog.domain.repository;

import com.vticket.eventcatalog.domain.entity.Seat;

import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    List<Seat> findByEventId(Long eventId);
    Optional<Seat> findById(Long id);
    List<Seat> findByIds(List<Long> seatIds);
    Seat save(Seat seat);
    void updateStatus(Long seatId, Seat.SeatStatus status);
}
