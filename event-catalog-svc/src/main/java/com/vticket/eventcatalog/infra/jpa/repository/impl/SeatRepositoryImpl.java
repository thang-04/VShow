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
        try {
            List<Seat> seats = seatEntityMapper.toDomainList(jpaRepository.findByEventId(eventId));
            log.info("{}|Found {} seats", prefix, seats.size());
            return seats;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding seats by event: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<Seat> findById(Long id) {
        String prefix = "[findById]|seat_id=" + id;
        try {
            Optional<Seat> seat = jpaRepository.findById(id)
                    .map(seatEntityMapper::toDomain);
            if (seat.isEmpty()) {
                log.warn("{}|No seat found", prefix);
            } else {
                log.info("{}|Seat found", prefix);
            }
            return seat;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding seat by id: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<Seat> findByIds(List<Long> seatIds) {
        String prefix = "[findByIds]|seat_ids=" + seatIds;
        try {
            List<Seat> seats = seatEntityMapper.toDomainList(jpaRepository.findByIdIn(seatIds));
            log.info("{}|Found {} seats", prefix, seats.size());
            return seats;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding seats by ids: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Seat save(Seat seat) {
        String prefix = "[save]|seat_id=" + (seat.getId() != null ? seat.getId() : "new");
        try {
            log.info("{}|Saving seat", prefix);
            SeatEntity saved = jpaRepository.save(seatEntityMapper.toEntity(seat));
            Seat result = seatEntityMapper.toDomain(saved);
            log.info("{}|Seat saved successfully|seat_id={}", prefix, result.getId());
            return result;
        } catch (Exception e) {
            log.error("{}|FAILED|Error saving seat: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void updateStatus(Long seatId, Seat.SeatStatus status) {
        String prefix = "[updateStatus]|seat_id=" + seatId + "|status=" + status;
        try {
            log.info("{}|Updating seat status", prefix);
            jpaRepository.updateStatus(seatId, status);
            log.info("{}|Seat status updated successfully", prefix);
        } catch (Exception e) {
            log.error("{}|FAILED|Error updating seat status: {}", prefix, e.getMessage(), e);
        }
    }
}
