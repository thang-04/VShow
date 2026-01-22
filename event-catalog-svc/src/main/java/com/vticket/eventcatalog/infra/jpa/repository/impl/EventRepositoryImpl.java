package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.app.dto.req.FilterRequest;
import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.jpa.EventEntity;
import com.vticket.eventcatalog.infra.jpa.repository.EventJpaRepository;
import com.vticket.eventcatalog.infra.jpa.mapper.EventEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
        try {
            log.info("{}|Saving event", prefix);
            EventEntity saved = jpaRepository.save(eventEntityMapper.toEntity(event));
            Event result = eventEntityMapper.toDomain(saved);
            log.info("{}|Event saved successfully|event_id={}", prefix, result.getId());
            return result;
        } catch (Exception e) {
            log.error("{}|FAILED|Error saving event: {}", prefix, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<Event> findById(Long id) {
        String prefix = "[findById]|event_id=" + id;
        try {
            Optional<Event> event = jpaRepository.findById(id)
                    .map(eventEntityMapper::toDomain);
            if (event.isEmpty()) {
                log.error("{}|FAILED|No data found", prefix);
            } else {
                log.info("{}|SUCCESS|Event found", prefix);
            }
            return event;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding event: {}", prefix, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Event> findAll() {
        String prefix = "[findAll]";
        try {
            List<Event> events = eventEntityMapper.toDomainList(jpaRepository.findAll());
            log.info("{}|Found {} events", prefix, events.size());
            return events;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding all events: {}", prefix, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public List<Event> findByCategoryId(Long categoryId) {
        String prefix = "[findByCategoryId]|category_id=" + categoryId;
        try {
            List<Event> events = eventEntityMapper.toDomainList(jpaRepository.findByCategoryId(categoryId));
            log.info("{}|Found {} events", prefix, events.size());
            return events;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding events by category: {}", prefix, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public List<Event> findActiveEvents() {
        String prefix = "[findActiveEvents]";
        try {
            List<Event> events = eventEntityMapper.toDomainList(jpaRepository.findByActiveTrue());
            log.info("{}|Found {} active events", prefix, events.size());
            return events;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding active events: {}", prefix, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public void deleteById(Long id) {
        String prefix = "[deleteById]|event_id=" + id;
        try {
            log.info("{}|Deleting event", prefix);
            jpaRepository.deleteById(id);
            log.info("{}|Event deleted successfully", prefix);
        } catch (Exception e) {
            log.error("{}|FAILED|Error deleting event: {}", prefix, e.getMessage(), e);
        }
    }

    @Override
    public List<Event> findAll(FilterRequest filter) {
        String prefix = "[findAll]|filter=" + filter;
        try {
            log.info("{}|Finding events with filter", prefix);
            Sort sort = Sort.unsorted();
            if (filter.getSortBy() != null && !filter.getSortBy().isEmpty()) {
                String[] parts = filter.getSortBy().split(",");
                if (parts.length > 0) {
                    String property = parts[0];
                    Direction direction = Direction.ASC;
                    if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
                        direction = Direction.DESC;
                    }
                    sort = Sort.by(direction, property);
                }
            }
            List<Long> categoryIds = filter.getCategoryIds();
            if (categoryIds != null && categoryIds.isEmpty()) {
                categoryIds = null;
            }
            List<EventEntity> entities = jpaRepository.searchEvents(
                    categoryIds,
                    filter.getLocation(),
                    filter.getMinPrice(),
                    filter.getMaxPrice(),
                    sort);
            List<Event> events = eventEntityMapper.toDomainList(entities);
            log.info("{}|Found {} events", prefix, events.size());
            return events;
        } catch (Exception e) {
            log.error("{}|FAILED|Error finding events with filter: {}", prefix, e.getMessage(), e);
            return null;
        }
    }
}
