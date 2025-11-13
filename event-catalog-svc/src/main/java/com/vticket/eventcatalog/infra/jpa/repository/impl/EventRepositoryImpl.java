package com.vticket.eventcatalog.infra.jpa.repository.impl;

import com.vticket.eventcatalog.domain.entity.Event;
import com.vticket.eventcatalog.domain.repository.EventRepository;
import com.vticket.eventcatalog.infra.jpa.EventEntity;
import com.vticket.eventcatalog.infra.jpa.repository.EventJpaRepository;
import com.vticket.eventcatalog.infra.jpa.mapper.EventEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final EventJpaRepository jpaRepository;
    private final EventEntityMapper eventEntityMapper;

    @Override
    public Event save(Event event) {
        EventEntity saved = jpaRepository.save(eventEntityMapper.toEntity(event));
        return eventEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return jpaRepository.findById(id)
                .map(eventEntityMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return eventEntityMapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public List<Event> findByCategoryId(Long categoryId) {
        return eventEntityMapper.toDomainList(jpaRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<Event> findActiveEvents() {
        return eventEntityMapper.toDomainList(jpaRepository.findByActiveTrue());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

