package com.vticket.eventcatalog.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketItemResponse {
    private Long id;
    private Long eventId;
    private String ticketName;
    private String description;
    private String color;
    private Boolean isFree;
    private Double price;
    private Double originalPrice;
    private Boolean isDiscount;
    private Integer discountPercent;
    private Double discountTotal;
    private Integer quantity;
    private List<SeatResponse> seats;
}
