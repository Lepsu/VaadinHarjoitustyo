package com.example.venue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VenueService {

    private final VenueRepository repository;

    public VenueService(VenueRepository repository) {
        this.repository = repository;
    }

    public List<Venue> findAll() {
        return repository.findAll();
    }

    public Optional<Venue> findById(Long id) {
        return repository.findById(id);
    }

    public Venue save(Venue venue) {
        return repository.save(venue);
    }

    public void delete(Venue venue) {
        repository.delete(venue);
    }
}