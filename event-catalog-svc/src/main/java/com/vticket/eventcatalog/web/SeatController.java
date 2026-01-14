package com.vticket.eventcatalog.web;

import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.eventcatalog.app.dto.req.SubmitTicketRequest;
import com.vticket.eventcatalog.app.dto.res.SeatResponse;
import com.vticket.eventcatalog.app.dto.res.SubmitTicketResponse;
import com.vticket.eventcatalog.app.mapper.SeatDtoMapper;
import com.vticket.eventcatalog.app.usercase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class SeatController {

    private final GetSeatsByEventUseCase getSeatsByEventUseCase;
    private final HoldSeatsUseCase holdSeatsUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final CheckSeatAvailabilityUseCase checkSeatAvailabilityUseCase;
    private final SubmitTicketUseCase submitTicketUseCase;
    private final SeatDtoMapper seatDtoMapper;

    @GetMapping("/{eventId}/seat-chart")
    public String getListSeats(@PathVariable("eventId") Long eventId) {
        long start = System.currentTimeMillis();
        try {
            var seats = getSeatsByEventUseCase.execute(eventId);
            List<SeatResponse> seatResponses = seatDtoMapper.toResponseList(seats);
            log.info("List Seat size: {}, Time taken: {}ms", seatResponses.size(), (System.currentTimeMillis() - start));
            return ResponseJson.success("List Seat", seatResponses);
        } catch (Exception e) {
            log.error("getListSeats|Exception|{}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching seats");
        }
    }

    @GetMapping("/{eventId}/seat/{seatId}/check")
    public String checkSeatHold(@PathVariable Long eventId, @PathVariable String seatId) {
        try {
            List<Long> seatIds = Arrays.stream(seatId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            var result = checkSeatAvailabilityUseCase.execute(eventId, seatIds);
            if (!result.isAvailable()) {
                if (result.getMissingSeatIds() != null && !result.getMissingSeatIds().isEmpty()) {
                    return ResponseJson.of(ErrorCode.SEAT_NOT_FOUND,
                            "Some seats not found in the database", result.getMissingSeatIds());
                }
                if (result.getHeldSeatIds() != null && !result.getHeldSeatIds().isEmpty()) {
                    return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                            "Some seats are currently held by another user", result.getHeldSeatIds());
                }
                if (result.getSoldSeatIds() != null && !result.getSoldSeatIds().isEmpty()) {
                    return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                            "Some seats are already sold", result.getSoldSeatIds());
                }
            }
            return ResponseJson.success("All seats available");
        } catch (Exception ex) {
            log.error("checkSeatHold|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error checking seat hold");
        }
    }

    @PostMapping("/{eventId}/seat/{seatId}/hold")
    public String holdSeat(@PathVariable Long eventId, @PathVariable String seatId) {
        try {
            List<Long> seatIds = Arrays.stream(seatId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            boolean success = holdSeatsUseCase.execute(eventId, seatIds);
            if (success) {
                return ResponseJson.success("Seats held successfully");
            } else {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                        "Some seats already held by another user");
            }
        } catch (Exception ex) {
            log.error("holdSeat|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while holding seats");
        }
    }

    @PostMapping("/{eventId}/booking/submit-ticket")
    public String submitTicket(@PathVariable Long eventId,
                               @RequestBody SubmitTicketRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("Received ticket submission request: {}", request);
        
        // Extract user ID from token (simplified - should use proper JWT service)
        String userId = extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Missing or invalid Authorization header");
        }

        // Ensure eventId matches
        request.setEventId(eventId);

        try {
            SubmitTicketResponse response = submitTicketUseCase.execute(request, userId);
            log.info("Ticket submission response: {}", response);
            return ResponseJson.success("Ticket submitted successfully", response);
        } catch (Exception ex) {
            log.error("submitTicket|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while submitting ticket");
        }
    }

    private String extractUserIdFromToken(String authorization) {
        // TODO: Implement proper JWT token extraction
        // For now, return a placeholder or extract from token
        if (authorization == null || authorization.isEmpty()) {
            return null;
        }
        // Remove "Bearer " prefix if present
        String token = authorization.replaceFirst("^Bearer\\s+", "");
        // TODO: Decode JWT and extract user ID
        // This should use a JWT service from identity-svc or common-libs
        return "user_" + token.hashCode(); // Placeholder
    }
}
