package com.vticket.eventcatalog.infra.jpa.repository;

import com.vticket.eventcatalog.infra.jpa.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventJpaRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByCategoryId(Long categoryId);
    List<EventEntity> findByActiveTrue();
}

