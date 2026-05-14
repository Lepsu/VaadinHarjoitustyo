package com.example.venue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByCityContainingIgnoreCase(String city);

    List<Venue> findByCapacityGreaterThanEqual(int capacity);
}