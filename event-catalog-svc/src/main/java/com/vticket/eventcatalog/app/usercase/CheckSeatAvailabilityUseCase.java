package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.domain.repository.SeatRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckSeatAvailabilityUseCase {

    private final SeatRepository seatRepository;
    private final RedisService redisService;

    public CheckSeatAvailabilityResult checkAvailability(Long eventId, List<Long> seatIds) {
        String prefix = "[CheckSeatAvailabilityUseCase]|eventId=" + eventId + "|seatIds=" + seatIds;
        try {
            // Check if seats exist in DB
            List<Seat> seats = seatRepository.findByIds(seatIds);
            List<Long> foundSeatIds = seats.stream()
                    .map(Seat::getId)
                    .toList();
            List<Long> missingSeatIds = seatIds.stream()
                    .filter(id -> !foundSeatIds.contains(id))
                    .collect(Collectors.toList());

            if (!missingSeatIds.isEmpty()) {
                log.error("{}|Seats not found in DB: {}", prefix, missingSeatIds);
                return CheckSeatAvailabilityResult.builder()
                        .available(false)
                        .missingSeatIds(missingSeatIds)
                        .build();
            }

            // Check if seats are held
            List<Long> heldSeats = redisService.getHoldSeatIds(eventId, seatIds);
            if (!heldSeats.isEmpty()) {
                log.error("{}|Some seats are currently held: {}", prefix, heldSeats);
                return CheckSeatAvailabilityResult.builder()
                        .available(false)
                        .heldSeatIds(heldSeats)
                        .build();
            }

            // Check if seats are available
            List<Long> soldSeats = seats.stream()
                    .filter(seat -> seat.getStatus() == Seat.SeatStatus.SOLD)
                    .map(Seat::getId)
                    .collect(Collectors.toList());

            if (!soldSeats.isEmpty()) {
                log.error("{}|Some seats are already sold: {}", prefix, soldSeats);
                return CheckSeatAvailabilityResult.builder()
                        .available(false)
                        .soldSeatIds(soldSeats)
                        .build();
            }

            log.info("{}|All seats available", prefix);
            return CheckSeatAvailabilityResult.builder()
                    .available(true)
                    .build();
        } catch (Exception ex) {
            log.error("{}|Exception: {}", prefix, ex.getMessage(), ex);
            return null;
        }
    }

    @Data
    @Builder
    public static class CheckSeatAvailabilityResult {
        private boolean available;
        private List<Long> missingSeatIds;
        private List<Long> heldSeatIds;
        private List<Long> soldSeatIds;
    }
}
