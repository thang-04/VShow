package com.vticket.eventcatalog.app.dto.req;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String venue;
    private Date startTime;
    private Date endTime;
    private Long categoryId;
}

