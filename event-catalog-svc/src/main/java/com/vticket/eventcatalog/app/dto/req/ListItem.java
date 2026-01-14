package com.vticket.eventcatalog.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListItem {
    private Long seatId;
    private String seatName;
    private String rowName;
    private Integer seatNumber;
    private Integer columnNumber;
    private Long ticketTypeId;
}
