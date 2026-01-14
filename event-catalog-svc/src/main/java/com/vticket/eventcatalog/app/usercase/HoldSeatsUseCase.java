package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldSeatsUseCase {

    private final RedisService redisService;
    private static final long SEAT_HOLD_TTL_MINUTES = 3;

    public boolean execute(Long eventId, List<Long> seatIds) {
        String prefix = "[HoldSeatsUseCase]|eventId=" + eventId + "|seatIds=" + seatIds;
        if (seatIds == null || seatIds.isEmpty()) {
            log.error("{}|Empty seat list", prefix);
            return false;
        }

        List<Long> sortedSeatIds = new ArrayList<>(seatIds);
        Collections.sort(sortedSeatIds);

        //Set order key redis for atomic ordering
        Long order = redisService.incrementOrderKey(eventId, sortedSeatIds);

        //Only first request
        if (order != null && order > 1) {
            log.error("{}|Rejected because another hold seat (order={})", prefix, order);
            redisService.deleteOrderKey(eventId, sortedSeatIds);
            return false;
        }

        try {
            //Check if seats already held
            List<Long> failedSeats = new ArrayList<>();
            for (Long seatId : sortedSeatIds) {
                if (redisService.checkSeatHold(eventId, seatId)) {
                    failedSeats.add(seatId);
                }
            }

            if (!failedSeats.isEmpty()) {
                log.error("{}|Some seats already held: {}", prefix, failedSeats);
                redisService.deleteOrderKey(eventId, sortedSeatIds);
                return false;
            }

            //Hold all seats
            long now = System.currentTimeMillis();
            for (Long seatId : sortedSeatIds) {
                boolean success = redisService.holdSeat(eventId, seatId, now);
                if (!success) {
                    log.error("{}|Failed to hold seat {}", prefix, seatId);
                    //Release already held seats
                    for (Long heldSeatId : sortedSeatIds) {
                        if (!heldSeatId.equals(seatId)) {
                            redisService.releaseSeat(eventId, heldSeatId);
                        }
                    }
                    redisService.deleteOrderKey(eventId, sortedSeatIds);
                    return false;
                }
            }

            //Update seat status hash to HOLD
            Map<String, String> holdUpdate = new HashMap<>();
            for (Long seatId : sortedSeatIds) {
                holdUpdate.put(seatId.toString(), com.vticket.eventcatalog.domain.entity.Seat.SeatStatus.HOLD.name());
            }
            redisService.updateSeatStatus(eventId, holdUpdate);

            log.info("{}|All {} seats held successfully (order={})", prefix, sortedSeatIds.size(), order);
            return true;

        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            //Release any held seats on error
            redisService.releaseSeats(eventId, sortedSeatIds);
            return false;
        } finally {
            redisService.deleteOrderKey(eventId, sortedSeatIds);
        }
    }
}
