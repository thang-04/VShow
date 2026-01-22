package com.vticket.eventcatalog.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {
    private List<Long> categoryIds;
    private String location;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;
}
