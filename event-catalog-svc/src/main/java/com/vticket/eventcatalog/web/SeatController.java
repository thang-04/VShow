package com.vticket.eventcatalog.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.eventcatalog.app.dto.req.CheckSeatAvailabilityRequest;
import com.vticket.eventcatalog.app.dto.req.HoldSeatsRequest;
import com.vticket.eventcatalog.app.dto.req.SubmitTicketRequest;
import com.vticket.eventcatalog.app.dto.res.SeatResponse;
import com.vticket.eventcatalog.app.dto.res.SubmitTicketResponse;
import com.vticket.eventcatalog.app.mapper.SeatDtoMapper;
import com.vticket.eventcatalog.app.usercase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SeatController {

    private final GetSeatsByEventUseCase getSeatsByEventUseCase;
    private final HoldSeatsUseCase holdSeatsUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final CheckSeatAvailabilityUseCase checkSeatAvailabilityUseCase;
    private final SubmitTicketUseCase submitTicketUseCase;
    private final SeatDtoMapper seatDtoMapper;

    @GetMapping("/api/seat-map")
    public String getSeatMap(@RequestParam(name = "eventId") Long eventId) {
        String prefix = "[getSeatMap]";
        long start = System.currentTimeMillis();
        log.info("{}|eventId: {}", prefix, eventId);
        try {
            var seats = getSeatsByEventUseCase.getSeatsByEventId(eventId);
            List<SeatResponse> seatResponses = seatDtoMapper.toResponseList(seats);
            log.info("{}|Seat map size: {}, Time taken: {}ms", prefix, seatResponses.size(),
                    (System.currentTimeMillis() - start));
            return ResponseJson.success("Seat map retrieved successfully", seatResponses);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching seat map");
        }
    }

    @PostMapping("/api/seats/check")
    public String checkSeatAvailability(@Valid @RequestBody CheckSeatAvailabilityRequest request) {
        String prefix = "[checkSeatAvailability]";
        log.info("{}|eventId: {}, seatIds: {}", prefix, request.getEventId(), request.getSeatIds());
        try {
            var result = checkSeatAvailabilityUseCase.checkAvailability(request.getEventId(), request.getSeatIds());
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
            return ResponseJson.success("All seats are available");
        } catch (Exception ex) {
            log.error("{}|Exception: {}", prefix, ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error checking seat availability");
        }
    }

    @PostMapping("/api/seats/hold")
    public String holdSeats(@Valid @RequestBody HoldSeatsRequest request) {
        String prefix = "[holdSeats]";
        log.info("{}|eventId: {}, seatIds: {}", prefix, request.getEventId(), request.getSeatIds());
        try {
            boolean success = holdSeatsUseCase.holdSeats(request.getEventId(), request.getSeatIds());
            if (success) {
                return ResponseJson.success("Seats held successfully");
            } else {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                        "Some seats are already held by another user");
            }
        } catch (Exception ex) {
            log.error("{}|Exception: {}", prefix, ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while holding seats");
        }
    }

    @PostMapping("/api/bookings/submit")
    public String submitTicket(@Valid @RequestBody SubmitTicketRequest request,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        String prefix = "[submitTicket]";
        log.info("{}|Received ticket submission request for eventId: {}", prefix, request.getEventId());

        String userId = extractUserIdFromToken(authorization);
        if (userId == null) {
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Missing or invalid Authorization header");
        }

        try {
            SubmitTicketResponse response = submitTicketUseCase.submitTicket(request, userId);
            log.info("{}|Ticket submission successful for booking code: {}", prefix, response.getBookingCode());
            return ResponseJson.success("Ticket submitted successfully", response);
        } catch (Exception ex) {
            log.error("{}|Exception: {}", prefix, ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while submitting ticket");
        }
    }

    private String extractUserIdFromToken(String authorization) {
        if (authorization == null || authorization.isEmpty()) {
            log.warn("[extractUserIdFromToken] Missing Authorization header");
            return null;
        }
        try {
            String token = authorization.trim();
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }

            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("[extractUserIdFromToken] Invalid JWT format - expected 3 parts, got {}", parts.length);
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            JsonObject jsonObject = new JsonParser().parse(payload).getAsJsonObject();
            if (jsonObject.has("jti")) {
                String userId = jsonObject.get("jti").getAsString();
                log.debug("[extractUserIdFromToken] Extracted userId: {}", userId);
                return userId;
            } else {
                log.error("[extractUserIdFromToken] 'jti' claim not found in token payload");
                return null;
            }
        } catch (Exception e) {
            log.error("[extractUserIdFromToken] Failed to extract userId: {}", e.getMessage(), e);
            return null;
        }
    }
}
