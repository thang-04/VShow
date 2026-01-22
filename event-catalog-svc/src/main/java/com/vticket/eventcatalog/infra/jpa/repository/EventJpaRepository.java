package com.vticket.eventcatalog.infra.jpa.repository;

import com.vticket.eventcatalog.infra.jpa.EventEntity;

import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByCategoryId(Long categoryId);

    List<EventEntity> findByActiveTrue();

    @Query("SELECT e FROM EventEntity e WHERE " +
            "(:categoryIds IS NULL OR e.categoryId IN :categoryIds) AND " +
            "(:location IS NULL OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR e.price <= :maxPrice) AND " +
            "e.active = true")
    List<EventEntity> searchEvents(@Param("categoryIds") List<Long> categoryIds,
            @Param("location") String location,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Sort sort);
}
