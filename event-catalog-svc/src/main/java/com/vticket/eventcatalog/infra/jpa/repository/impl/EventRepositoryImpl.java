package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.jpa.EventEntity;
import com.vticket.eventcatalog.infra.jpa.repository.EventJpaRepository;
import com.vticket.eventcatalog.infra.jpa.mapper.EventEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final EventJpaRepository jpaRepository;
    private final EventEntityMapper eventEntityMapper;

    @Override
    public Event save(Event event) {
        String prefix = "[save]|event_id=" + (event.getId() != null ? event.getId() : "new");
        log.info("{}|Saving event", prefix);
        EventEntity saved = jpaRepository.save(eventEntityMapper.toEntity(event));
        Event result = eventEntityMapper.toDomain(saved);
        log.info("{}|Event saved successfully|event_id={}", prefix, result.getId());
        return result;
    }

    @Override
    public Optional<Event> findById(Long id) {
        String prefix = "[findById]|event_id=" + id;
        Optional<Event> event = jpaRepository.findById(id)
                .map(eventEntityMapper::toDomain);
        if (event.isEmpty()) {
            log.error("{}|FAILED|No data found", prefix);
        } else {
            log.info("{}|SUCCESS|Event found", prefix);
        }
        return event;
    }

    @Override
    public List<Event> findAll() {
        return eventEntityMapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public List<Event> findByCategoryId(Long categoryId) {
        String prefix = "[findByCategoryId]|category_id=" + categoryId;
        List<Event> events = eventEntityMapper.toDomainList(jpaRepository.findByCategoryId(categoryId));
        log.info("{}|Found {} events", prefix, events.size());
        return events;
    }

    @Override
    public List<Event> findActiveEvents() {
        return eventEntityMapper.toDomainList(jpaRepository.findByActiveTrue());
    }

    @Override
    public void deleteById(Long id) {
        String prefix = "[deleteById]|event_id=" + id;
        log.info("{}|Deleting event", prefix);
        jpaRepository.deleteById(id);
        log.info("{}|Event deleted successfully", prefix);
    }
}

