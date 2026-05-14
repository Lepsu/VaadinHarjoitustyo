package com.example.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Haku nimen perusteella (osittainen)
    List<Event> findByNameContainingIgnoreCase(String name);

    // Haku kaupungin perusteella (JOIN venue)
    @Query("SELECT e FROM Event e JOIN e.venue v WHERE LOWER(v.city) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Event> findByVenueCity(@Param("city") String city);
}