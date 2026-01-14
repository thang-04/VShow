package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.domain.repository.SeatRepository;
import com.vticket.eventcatalog.infra.jpa.SeatEntity;
import com.vticket.eventcatalog.infra.jpa.mapper.SeatEntityMapper;
import com.vticket.eventcatalog.infra.jpa.repository.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepository {

    private final SeatJpaRepository jpaRepository;
    private final SeatEntityMapper seatEntityMapper;

    @Override
    public List<Seat> findByEventId(Long eventId) {
        String prefix = "[findByEventId]|event_id=" + eventId;
        List<Seat> seats = seatEntityMapper.toDomainList(jpaRepository.findByEventId(eventId));
        log.info("{}|Found {} seats", prefix, seats.size());
        return seats;
    }

    @Override
    public Optional<Seat> findById(Long id) {
        String prefix = "[findById]|seat_id=" + id;
        Optional<Seat> seat = jpaRepository.findById(id)
                .map(seatEntityMapper::toDomain);
        if (seat.isEmpty()) {
            log.warn("{}|No seat found", prefix);
        } else {
            log.info("{}|Seat found", prefix);
        }
        return seat;
    }

    @Override
    public List<Seat> findByIds(List<Long> seatIds) {
        String prefix = "[findByIds]|seat_ids=" + seatIds;
        List<Seat> seats = seatEntityMapper.toDomainList(jpaRepository.findByIdIn(seatIds));
        log.info("{}|Found {} seats", prefix, seats.size());
        return seats;
    }

    @Override
    public Seat save(Seat seat) {
        String prefix = "[save]|seat_id=" + (seat.getId() != null ? seat.getId() : "new");
        log.info("{}|Saving seat", prefix);
        SeatEntity saved = jpaRepository.save(seatEntityMapper.toEntity(seat));
        Seat result = seatEntityMapper.toDomain(saved);
        log.info("{}|Seat saved successfully|seat_id={}", prefix, result.getId());
        return result;
    }

    @Override
    public void updateStatus(Long seatId, Seat.SeatStatus status) {
        String prefix = "[updateStatus]|seat_id=" + seatId + "|status=" + status;
        log.info("{}|Updating seat status", prefix);
        jpaRepository.updateStatus(seatId, status);
        log.info("{}|Seat status updated successfully", prefix);
    }
}
