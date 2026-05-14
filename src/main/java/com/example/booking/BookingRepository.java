package com.example.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.event")
    List<Booking> findAllWithEvent();

    List<Booking> findByEvent_Id(Long eventId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByBookerEmailContainingIgnoreCase(String email);
    boolean existsByReferenceNumber(String referenceNumber);
}