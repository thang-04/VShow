package com.vticket.eventcatalog.domain.repository;

import com.vticket.eventcatalog.domain.entity.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(Long id);
    List<Event> findAll();
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findActiveEvents();
    void deleteById(Long id);
}

