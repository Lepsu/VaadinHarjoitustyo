package com.example.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public List<Event> findAll() {
        return repository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return repository.findById(id);
    }

    public List<Event> findByName(String name) {
        if (name == null || name.isBlank()) {
            return repository.findAll();
        }
        return repository.findByNameContainingIgnoreCase(name);
    }

    public Event save(Event event) {
        return repository.save(event);
    }

    public void delete(Event event) {
        repository.delete(event);
    }

    public long count() {
        return repository.count();
    }
}