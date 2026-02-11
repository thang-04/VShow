package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.domain.repository.SeatRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSeatsByEventUseCase {

    private final SeatRepository seatRepository;
    private final RedisService redisService;

    public List<Seat> getSeatsByEventId(Long eventId) {
        long start = System.currentTimeMillis();
        String prefix = "[GetSeatsByEventUseCase]|eventId=" + eventId;
        try {
            Map<Object, Object> seatStatusMap = redisService.getSeatStatusHash(eventId);

            // Get seat list from DB
            List<Seat> seatList = seatRepository.findByEventId(eventId);
            if (seatList == null || seatList.isEmpty()) {
                log.error("{}|No seats found for eventId", prefix);
                return List.of();
            }

            // Cache seat status if not exist
            if (seatStatusMap.isEmpty()) {
                Map<String, String> hashData = new HashMap<>();
                for (Seat seat : seatList) {
                    hashData.put(seat.getId().toString(), seat.getStatus().name());
                }
                redisService.cacheSeatStatus(eventId, hashData);
                seatStatusMap = new HashMap<>(hashData);
                log.info("{}|Cached seat status for eventId", prefix);
            }

            // Check which seats are held and update status
            Map<String, String> holdUpdate = new HashMap<>();
            for (Seat seat : seatList) {
                if (redisService.checkSeatHold(eventId, seat.getId())) {
                    holdUpdate.put(seat.getId().toString(), Seat.SeatStatus.HOLD.name());
                }
            }
            if (!holdUpdate.isEmpty()) {
                redisService.updateSeatStatus(eventId, holdUpdate);
                //reload seatStatus
                seatStatusMap.putAll(holdUpdate);
            }

            // Update seat status from Redis
            for (Seat seat : seatList) {
                Object statusObj = seatStatusMap.get(seat.getId().toString());
                if (statusObj != null) {
                    try {
                        Seat.SeatStatus status = Seat.SeatStatus.valueOf(statusObj.toString());
                        seat.setStatus(status);
                    } catch (IllegalArgumentException e) {
                        log.error("{}|Invalid status for seat {}: {}", prefix, seat.getId(), statusObj);
                    }
                }
            }

            // log.info("{}|Success|fromRedis={}|holdCount={}|size={}|time={}ms", prefix,
            // !seatStatusMap.isEmpty(),
            // (holdSeatIds != null ? holdSeatIds.size() : 0), seatList.size(),
            // (System.currentTimeMillis() - start));
            return seatList;
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
            return List.of();
        }
    }
}
