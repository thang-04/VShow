package com.vticket.eventcatalog.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String slug;
    private String title;
    private String description;
    private BigDecimal price;
    private String venue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
}
