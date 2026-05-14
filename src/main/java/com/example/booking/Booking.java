package com.example.booking;

import com.example.event.Event;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Audited
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Varaajan nimi on pakollinen")
    @Size(min = 2, max = 100)
    private String bookerName;

    @Email(message = "Virheellinen sähköposti")
    @NotBlank(message = "Sähköposti on pakollinen")
    private String bookerEmail;

    @Min(value = 1, message = "Vähintään 1 paikka")
    @Max(value = 50, message = "Enintään 50 paikkaa kerralla")
    private int numberOfSeats;

    @NotNull(message = "Varausaika on pakollinen")
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status on pakollinen")
    private BookingStatus status = BookingStatus.PENDING;

    @NotBlank(message = "Viitenumero on pakollinen")
    @Size(min = 5, max = 20)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    // Getterit ja setterit
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }

    public String getBookerEmail() { return bookerEmail; }
    public void setBookerEmail(String bookerEmail) { this.bookerEmail = bookerEmail; }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}