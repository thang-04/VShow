package com.vticket.eventcatalog.infra.jpa.repository;

import com.vticket.eventcatalog.infra.jpa.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {
    Optional<BookingEntity> findByBookingCode(String bookingCode);
}
