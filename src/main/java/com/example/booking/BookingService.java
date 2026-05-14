package com.example.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public List<Booking> findAll() {
        return repository.findAllWithEvent();
    }

    public Optional<Booking> findById(Long id) {
        return repository.findById(id);
    }

    public List<Booking> findByEvent(Long eventId) {
        return repository.findByEvent_Id(eventId);
    }

    public Booking save(Booking booking) {
        // Generoi viitenumero automaattisesti jos puuttuu
        if (booking.getReferenceNumber() == null
                || booking.getReferenceNumber().isBlank()) {
            booking.setReferenceNumber(generateReference());
        }
        if (booking.getBookingTime() == null) {
            booking.setBookingTime(LocalDateTime.now());
        }
        return repository.save(booking);
    }

    public void delete(Booking booking) {
        repository.delete(booking);
    }

    private String generateReference() {
        return "EVT-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
    }
}