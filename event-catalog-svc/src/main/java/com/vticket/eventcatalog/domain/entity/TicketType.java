package com.vticket.eventcatalog.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TicketType {
    private Long id;
    private Long event_id;
    private String ticket_name;
    private String description;
    private String color;
    private Boolean is_free;
    private Double price;
    private Double original_price;
    private Integer max_qty_per_order;
    private Integer min_qty_per_order;
    private Date start_time;
    private Date end_time;
    private Integer position;
    private Integer ticket_status;
    private String image_url;
    private Boolean is_discount;
    private Integer discount_percent;
    private Date time_discount_start;
    private Date time_discount_end;
    private Integer quantity;
    private Integer availableQuantity;
}

