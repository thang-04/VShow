package com.vticket.eventcatalog.web;

import com.vticket.commonlibs.exception.AppException;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.eventcatalog.app.dto.req.CheckSeatAvailabilityRequest;
import com.vticket.eventcatalog.app.dto.req.HoldSeatsRequest;
import com.vticket.eventcatalog.app.dto.req.SubmitTicketRequest;
import com.vticket.eventcatalog.app.dto.res.SeatResponse;
import com.vticket.eventcatalog.app.dto.res.SubmitTicketResponse;
import com.vticket.eventcatalog.app.mapper.SeatDtoMapper;
import com.vticket.eventcatalog.app.usercase.*;
import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.domain.repository.EventRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class SeatController {

    private final GetSeatsByEventUseCase getSeatsByEventUseCase;
    private final HoldSeatsUseCase holdSeatsUseCase;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final CheckSeatAvailabilityUseCase checkSeatAvailabilityUseCase;
    private final SubmitTicketUseCase submitTicketUseCase;
    private final SeatDtoMapper seatDtoMapper;
    private final EventRepository eventRepository;

    // @GetMapping("/seat-map")
    // public String getSeatMap(@RequestParam(name = "eventId") Long eventId) {
    // String prefix = "[getSeatMap]";
    // long start = System.currentTimeMillis();
    // log.info("{}|eventId: {}", prefix, eventId);
    // try {
    // List<Seat> seats = getSeatsByEventUseCase.getSeatsByEventId(eventId);
    // List<SeatResponse> seatResponses = seatDtoMapper.toResponseList(seats);
    // log.info("{}|Seat map size: {}, Time taken: {}ms", prefix,
    // seatResponses.size(),
    // (System.currentTimeMillis() - start));
    // return ResponseJson.success("Seat map retrieved successfully",
    // seatResponses);
    // } catch (Exception e) {
    // log.error("{}|Exception: {}", prefix, e.getMessage(), e);
    // return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred
    // while fetching seat map");
    // }
    // }

    @GetMapping("/{slug}/seat-map")
    public String getSeatMapBySlug(@PathVariable("slug") String slug) {
        String prefix = "[getSeatMapBySlug]";
        long start = System.currentTimeMillis();
        log.info("{}|slug: {}", prefix, slug);
        try {
            // Find event by slug to get eventId
            var event = eventRepository.findBySlug(slug)
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            Long eventId = event.getId();
            log.info("{}|Resolved slug '{}' to eventId: {}", prefix, slug, eventId);

            List<Seat> seats = getSeatsByEventUseCase.getSeatsByEventId(eventId);
            List<SeatResponse> seatResponses = seatDtoMapper.toResponseList(seats);
            log.info("{}|Seat map size: {}, Time taken: {}ms", prefix, seatResponses.size(),
                    (System.currentTimeMillis() - start));
            return ResponseJson.success("Seat map retrieved successfully", seatResponses);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching seat map");
        }
    }

    @PostMapping("/seats/check")
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

    @PostMapping("/seats/hold")
    public String holdSeats(@Valid @RequestBody HoldSeatsRequest request) {
        String prefix = "[holdSeats]";
        log.info("{}|eventId: {}, seatIds: {}", prefix, request.getEventId(), request.getSeatIds());
        try {
            String bookId = holdSeatsUseCase.holdSeats(request.getEventId(), request.getSeatIds());
            if (bookId != null) {
                return ResponseJson.success("Seats held successfully", bookId);
            } else {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE, "Some seats are already held by another user");
            }
        } catch (Exception ex) {
            log.error("{}|Exception: {}", prefix, ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while holding seats");
        }
    }

    @PostMapping("/bookings/submit")
    public String submitTicket(@Valid @RequestBody SubmitTicketRequest request,
                               @RequestHeader("X-USER-ID") String userId) {
        String prefix = "[submitTicket]";
        log.info("{}|Received ticket submission request for eventId: {}", prefix, request.getEventId());
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
}
