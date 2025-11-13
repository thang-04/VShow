package com.vticket.eventcatalog.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SeatMeta {
    private Integer totalSeats;
    private Integer rows;
    private Integer seatsPerRow;
    private String layout;
}

