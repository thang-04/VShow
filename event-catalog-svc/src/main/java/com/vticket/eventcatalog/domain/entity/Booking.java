package com.vticket.eventcatalog.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private String bookingCode;
    private String userId;
    private Long eventId;
    private String seatIds; // comma-separated
    private Double subtotal;
    private Double totalAmount;
    private PaymentMethod paymentMethod;
    private BookingStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED
    }

    public enum PaymentMethod {
        MOMO, VNPAY, BANK_TRANSFER
    }
}
