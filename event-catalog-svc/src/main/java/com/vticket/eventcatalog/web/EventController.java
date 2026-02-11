package com.vticket.eventcatalog.web;

import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.eventcatalog.app.dto.req.CreateEventRequest;
import com.vticket.eventcatalog.app.dto.req.FilterRequest;
import com.vticket.eventcatalog.app.dto.req.UpdateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;

import com.vticket.eventcatalog.app.usercase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final ListEventUseCase listEventUseCase;
    private final GetEventUseCase getEventUseCase;

    @GetMapping
    public String listEvents(
            @RequestParam(name = "categoryIds", required = false) String categoryIds,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sortBy", required = false) String sortBy) {
        String prefix = "[listEvents]";
        log.info("{}|Request params - categoryIds: {}, location: {}, minPrice: {}, maxPrice: {}, sortBy: {}",
                prefix, categoryIds, location, minPrice, maxPrice, sortBy);
        try {
            List<Long> lstCategoryIds = new ArrayList<>();
            if (categoryIds != null && !categoryIds.isEmpty()) {
                Arrays.stream(categoryIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .forEach(lstCategoryIds::add);
            }
            log.info("{}|Parsed category IDs: {}", prefix, lstCategoryIds);
            List<EventResponse> events;
            if (!lstCategoryIds.isEmpty() || location != null || minPrice != null
                    || maxPrice != null || sortBy != null) {
                FilterRequest filter = FilterRequest
                        .builder()
                        .categoryIds(lstCategoryIds)
                        .location(location)
                        .minPrice(minPrice)
                        .maxPrice(maxPrice)
                        .sortBy(sortBy)
                        .build();
                events = listEventUseCase.getEventsWithFilter(filter);
            } else {
                events = listEventUseCase.getAllEvents();
            }
            return ResponseJson.success("List of events", events);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, e.getMessage());
        }
    }

    // @GetMapping("/{id}")
    // public String getEvent(@PathVariable("id") Long id) {
    // String prefix = "[getEvent]";
    // log.info("{}|Request params - id: {}", prefix, id);
    // try {
    // EventResponse event = getEventUseCase.getEventById(id);
    // return ResponseJson.success("Event details", event);
    // } catch (Exception e) {
    // log.error("{}|Exception: {}", prefix, e.getMessage(), e);
    // return ResponseJson.of(ErrorCode.EVENT_NOT_FOUND, e.getMessage());
    // }
    // }

    @GetMapping("/{slug}")
    public String getEventBySlug(@PathVariable("slug") String slug) {
        String prefix = "[getEventBySlug]";
        log.info("{}|Request params - slug: {}", prefix, slug);
        try {
            EventResponse event = getEventUseCase.getEventBySlug(slug);
            return ResponseJson.success("Event details", event);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.EVENT_NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public String createEvent(@Valid @RequestBody CreateEventRequest request) {
        String prefix = "[createEvent]";
        log.info("{}|Request body: {}", prefix, request);
        try {
            EventResponse event = createEventUseCase.createEvent(request);
            return ResponseJson.success("Event created successfully", event);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateEvent(@PathVariable("id") Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        String prefix = "[updateEvent]";
        log.info("{}|Request params - id: {}, body: {}", prefix, id, request);
        try {
            EventResponse event = updateEventUseCase.updateEvent(id, request);
            return ResponseJson.success("Event updated successfully", event);
        } catch (Exception e) {
            log.error("{}|Exception: {}", prefix, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.EVENT_NOT_FOUND, e.getMessage());
        }
    }
}
