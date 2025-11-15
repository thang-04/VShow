package com.vticket.eventcatalog.web;

import com.vticket.commonlibs.utils.ResponseJson;
import com.vticket.commonlibs.exception.ErrorCode;
import com.vticket.eventcatalog.app.dto.req.CreateEventRequest;
import com.vticket.eventcatalog.app.dto.res.EventResponse;
import com.vticket.eventcatalog.app.dto.req.UpdateEventRequest;
import com.vticket.eventcatalog.app.usercase.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public String listEvents(@RequestParam(name = "categoryId",required = false) Long categoryId) {
        try {
            List<EventResponse> events;
            if (categoryId != null) {
                events = listEventUseCase.executeByCategory(categoryId);
            } else {
                events = listEventUseCase.execute();
            }
            return ResponseJson.success("List of events", events);
        } catch (Exception e) {
            log.error("Failed to list events: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String getEvent(@PathVariable("id") Long id) {
        try {
            EventResponse event = getEventUseCase.execute(id);
            return ResponseJson.success("Event details", event);
        } catch (Exception e) {
            log.error("Failed to get event: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.EVENT_NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public String createEvent(@Valid @RequestBody CreateEventRequest request) {
        try {
            log.info("Create event request: {}", request.getTitle());
            EventResponse event = createEventUseCase.execute(request);
            return ResponseJson.success("Event created successfully", event);
        } catch (Exception e) {
            log.error("Failed to create event: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.INVALID_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public String updateEvent(@PathVariable("id") Long id,
                              @Valid @RequestBody UpdateEventRequest request) {
        try {
            log.info("Update event request for id: {}", id);
            EventResponse event = updateEventUseCase.execute(id, request);
            return ResponseJson.success("Event updated successfully", event);
        } catch (Exception e) {
            log.error("Failed to update event: {}", e.getMessage(), e);
            return ResponseJson.of(ErrorCode.EVENT_NOT_FOUND, e.getMessage());
        }
    }
}

