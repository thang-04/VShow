package com.vticket.eventcatalog.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private Long id;
    private Long ticketTypeId;
    private String seatName;
    private Integer seatNumber;
    private String rowName;
    private Integer columnNumber;
    private String status;
    private Double price;
}
