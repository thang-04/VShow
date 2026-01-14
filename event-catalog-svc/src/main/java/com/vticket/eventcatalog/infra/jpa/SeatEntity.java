package com.vticket.eventcatalog.infra.jpa;

import com.vticket.eventcatalog.domain.entity.Seat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "seat_name")
    private String seatName;

    @Column(name = "row_name")
    private String rowName;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "column_number")
    private Integer columnNumber;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Seat.SeatStatus status;

    @Column(nullable = false)
    private Double price;

    @Column(name = "ticket_type_id")
    private Long ticketTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", insertable = false, updatable = false)
    private TicketTypeEntity ticketType;
}
