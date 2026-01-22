package com.vticket.eventcatalog.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsRequest {
    private Long eventId;
    private List<Long> seatIds;
}
