package com.vticket.eventcatalog.domain.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    private Long id;
    private Long eventId;
    private String seatName;
    private String rowName;
    private Integer seatNumber;
    private Integer columnNumber;
    private SeatStatus status;
    private Double price;
    private Long ticketTypeId;
    private TicketType ticketType;

    public enum SeatStatus {
        AVAILABLE, HOLD, SOLD
    }
}
