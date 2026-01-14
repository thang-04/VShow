package com.vticket.eventcatalog.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotNull(message = "Start time is required")
    private Date startTime;

    @NotNull(message = "End time is required")
    private Date endTime;

    private Long categoryId;
}

