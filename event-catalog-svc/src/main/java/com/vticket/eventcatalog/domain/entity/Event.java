package com.vticket.eventcatalog.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String venue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long categoryId;
    private List<TicketType> ticketTypes;
    private SeatMeta seatMeta;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    public void update(String title, String description, BigDecimal price, String venue,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.updatedAt = LocalDateTime.now();
    }
}

