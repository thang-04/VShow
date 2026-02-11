package com.vticket.eventcatalog.domain.repository;

import com.vticket.eventcatalog.app.dto.req.FilterRequest;
import com.vticket.eventcatalog.domain.entity.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);

    Optional<Event> findById(Long id);

    Optional<Event> findBySlug(String slug);

    List<Event> findAll();

    List<Event> findByCategoryId(Long categoryId);

    List<Event> findActiveEvents();

    List<Event> findAll(FilterRequest filter);

    void deleteById(Long id);
}
