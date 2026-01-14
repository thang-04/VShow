package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseSeatsUseCase {

    private final RedisService redisService;

    public void execute(Long eventId, List<Long> seatIds) {
        String prefix = "[ReleaseSeatsUseCase]|eventId=" + eventId + "|seatIds=" + seatIds;

        try {
            redisService.releaseSeats(eventId, seatIds);

            //Update seat status back to AVAILABLE
            Map<String, String> availableUpdate = new HashMap<>();
            for (Long seatId : seatIds) {
                availableUpdate.put(seatId.toString(), Seat.SeatStatus.AVAILABLE.name());
            }
            redisService.updateSeatStatus(eventId, availableUpdate);

            log.info("{}|Released {} seats successfully", prefix, seatIds.size());
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
        }
    }
}
