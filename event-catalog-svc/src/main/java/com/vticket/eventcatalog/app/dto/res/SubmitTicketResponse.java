package com.vticket.eventcatalog.app.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitTicketResponse {
    private Long eventId;
    private String bookingCode;
    private Long bookingId;
    private String discountCode;
    private List<TicketItemResponse> listItem;
    private Double subtotal;
    private Double totalAmount;
    private String paymentCode;
    private Long expiredAt;
}
