package com.vticket.eventcatalog.app.usercase;

import com.vticket.eventcatalog.app.dto.req.ListItem;
import com.vticket.eventcatalog.app.dto.req.SubmitTicketRequest;
import com.vticket.eventcatalog.app.dto.res.SeatResponse;
import com.vticket.eventcatalog.app.dto.res.SubmitTicketResponse;
import com.vticket.eventcatalog.app.dto.res.TicketItemResponse;
import com.vticket.eventcatalog.app.mapper.SeatDtoMapper;
import com.vticket.eventcatalog.domain.entity.Booking;
import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.domain.entity.TicketType;
import com.vticket.eventcatalog.domain.repository.BookingRepository;
import com.vticket.eventcatalog.domain.repository.SeatRepository;
import com.vticket.eventcatalog.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitTicketUseCase {

    private static final long BOOKING_TIME_MINUTES = 5;

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final RedisService redisService;
    private final HoldSeatsUseCase holdSeatsUseCase;
    private final SeatDtoMapper seatDtoMapper;

    @Transactional
    public SubmitTicketResponse execute(SubmitTicketRequest request, String userId) {
        String prefix = "[SubmitTicketUseCase]|eventId=" + request.getEventId();
        long start = System.currentTimeMillis();
        String bookingCode = UUID.randomUUID().toString();
        try {
            List<Long> requestSeatIds = request.getListItem().stream()
                    .map(ListItem::getSeatId)
                    .collect(Collectors.toList());

            //Check if seats exist
            List<Seat> seats = seatRepository.findByIds(requestSeatIds);
            List<Long> foundSeatIds = seats.stream()
                    .map(Seat::getId)
                    .toList();
            List<Long> missingSeatIds = requestSeatIds.stream()
                    .filter(id -> !foundSeatIds.contains(id))
                    .collect(Collectors.toList());

            if (!missingSeatIds.isEmpty()) {
                log.error("{}|Seats not found in DB: {}", prefix, missingSeatIds);
                return null;

            }

            //Check seat availability
            for (Seat seat : seats) {
                if (seat.getStatus() == Seat.SeatStatus.SOLD) {
                    log.error("{}|Some seats not available (SOLD)", prefix);
                    return null;
                }
            }

            //Hold seats if not already held
            List<Long> heldSeats = redisService.getHoldSeatIds(request.getEventId(), requestSeatIds);
            List<Long> seatsToHold = requestSeatIds.stream()
                    .filter(id -> !heldSeats.contains(id))
                    .collect(Collectors.toList());

            if (!seatsToHold.isEmpty()) {
                boolean holdSuccess = holdSeatsUseCase.execute(request.getEventId(), seatsToHold);
                if (!holdSuccess) {
                    log.error("{}|Failed to hold seats", prefix);
                    return null;
                }
            }

            Double totalAmount = 0.0;
            for (Seat seat : seats) {
                totalAmount += seat.getPrice();
            }

            //Group seats by ticket type
            Map<Long, List<Seat>> groupedSeats = seats.stream()
                    .collect(Collectors.groupingBy(Seat::getTicketTypeId));

            //Build ticket items response
            List<TicketItemResponse> ticketItems = new ArrayList<>();
            for (Map.Entry<Long, List<Seat>> entry : groupedSeats.entrySet()) {
                List<Seat> seatList = entry.getValue();
                Seat firstSeat = seatList.get(0);
                
                //Get ticket type info from seat
                TicketType ticketType = firstSeat.getTicketType();
                if (ticketType == null) {
                    //Fallback: create minimal ticket type info
                    ticketType = TicketType.builder()
                            .id(firstSeat.getTicketTypeId())
                            .ticket_name("Ticket Type " + firstSeat.getTicketTypeId())
                            .build();
                }

                List<SeatResponse> seatResponses = seatList.stream().map(seat -> {
                    SeatResponse s = new SeatResponse();
                    s.setId(seat.getId());
                    s.setTicketTypeId(seat.getTicketTypeId());
                    s.setSeatName(seat.getSeatName());
                    s.setSeatNumber(seat.getSeatNumber());
                    s.setRowName(seat.getRowName());
                    s.setColumnNumber(seat.getColumnNumber());
                    return s;
                }).collect(Collectors.toList());

                TicketItemResponse item = new TicketItemResponse();
                item.setId(ticketType.getId());
                item.setEventId(request.getEventId());
                item.setTicketName(ticketType.getTicket_name());
                item.setColor(ticketType.getColor());
                item.setIsFree(ticketType.getIs_free());
                item.setPrice(ticketType.getPrice());
                item.setOriginalPrice(ticketType.getOriginal_price());
                item.setIsDiscount(ticketType.getIs_discount());
                item.setDiscountPercent(ticketType.getDiscount_percent());
                item.setQuantity(seatList.size());
                item.setSeats(seatResponses);
                ticketItems.add(item);
            }

            //Create booking
            Booking booking = Booking.builder()
                    .bookingCode(bookingCode)
                    .userId(userId)
                    .eventId(request.getEventId())
                    .seatIds(requestSeatIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")))
                    .subtotal(totalAmount)
                    .totalAmount(totalAmount)
                    .paymentMethod(Booking.PaymentMethod.MOMO) //Default
                    .status(Booking.BookingStatus.PENDING)
                    .expiredAt(LocalDateTime.now().plusMinutes(BOOKING_TIME_MINUTES))
                    .build();

            Booking savedBooking = bookingRepository.save(booking);
            log.info("{}|Inserted booking with ID: {} for booking code: {}", prefix, savedBooking.getId(), bookingCode);

            //Update Redis seat status to SOLD
            try {
                Map<String, String> updateMap = new HashMap<>();
                for (Long seatId : requestSeatIds) {
                    updateMap.put(seatId.toString(), Seat.SeatStatus.SOLD.name());
                }
                redisService.updateSeatStatus(request.getEventId(), updateMap);

                //Remove from seat-hold zset
                redisService.releaseSeats(request.getEventId(), requestSeatIds);

                log.info("{}|Updated Redis Hash and cleaned ZSET for eventId, seats={}", prefix, requestSeatIds);
            } catch (Exception e) {
                log.error("{}|Redis update failed|error={}", prefix, e.getMessage());
            }

            //Update seat status in DB
            for (Long seatId : requestSeatIds) {
                seatRepository.updateStatus(seatId, Seat.SeatStatus.SOLD);
            }

            //Build response
            SubmitTicketResponse response = new SubmitTicketResponse();
            response.setEventId(request.getEventId());
            response.setBookingCode(bookingCode);
            response.setBookingId(savedBooking.getId());
            response.setDiscountCode("");
            response.setListItem(ticketItems);
            response.setPaymentCode("MOMO"); //Default
            response.setSubtotal(totalAmount);
            response.setTotalAmount(totalAmount);
            response.setExpiredAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(BOOKING_TIME_MINUTES));

            log.info("{}|Success|Booking code: {}|Time: {} ms", prefix, bookingCode, (System.currentTimeMillis() - start));
            return response;
        } catch (Exception ex) {
            log.error("{}|Exception|{}", prefix, ex.getMessage(), ex);
           return null;
        }
    }
}
