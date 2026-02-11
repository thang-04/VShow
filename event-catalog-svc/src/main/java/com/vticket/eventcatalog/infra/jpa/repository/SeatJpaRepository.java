package com.vticket.eventcatalog.infra.jpa.repository;

import com.vticket.eventcatalog.domain.entity.Seat;
import com.vticket.eventcatalog.infra.jpa.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByEventIdOrderByRowName(Long eventId);

    List<SeatEntity> findByIdIn(List<Long> ids);

    @Modifying
    @Query("UPDATE SeatEntity s SET s.status = :status WHERE s.id = :seatId")
    void updateStatus(@Param("seatId") Long seatId, @Param("status") Seat.SeatStatus status);
}
