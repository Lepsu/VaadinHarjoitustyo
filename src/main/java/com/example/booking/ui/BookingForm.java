package com.example.booking.ui;

import com.example.booking.Booking;
import com.example.booking.BookingStatus;
import com.example.event.Event;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.time.LocalDateTime;
import java.util.List;

public class BookingForm extends FormLayout {

    private final TextField bookerName = new TextField("Varaajan nimi");
    private final EmailField bookerEmail = new EmailField("Sähköposti");
    private final IntegerField numberOfSeats = new IntegerField("Paikkoja");
    private final DateTimePicker bookingTime =
            new DateTimePicker("Varausaika");
    private final ComboBox<BookingStatus> status =
            new ComboBox<>("Status");
    private final TextField referenceNumber =
            new TextField("Viitenumero");
    private final ComboBox<Event> event = new ComboBox<>("Tapahtuma");

    private final Button save = new Button("Tallenna");
    private final Button delete = new Button("Poista");
    private final Button cancel = new Button("Peruuta");

    private final Binder<Booking> binder = new Binder<>(Booking.class);

    public BookingForm(List<Event> events) {
        addClassName("booking-form");

        event.setItems(events);
        event.setItemLabelGenerator(Event::getName);
        event.setPlaceholder("-- Valitse tapahtuma --");

        status.setItems(BookingStatus.values());
        status.setItemLabelGenerator(s -> switch (s) {
            case PENDING -> "Odottaa";
            case CONFIRMED -> "Vahvistettu";
            case CANCELLED -> "Peruutettu";
        });

        numberOfSeats.setMin(1);
        numberOfSeats.setMax(50);

        referenceNumber.setReadOnly(true);
        referenceNumber.setPlaceholder("Generoidaan automaattisesti");

        // Oletusarvot
        status.setValue(BookingStatus.PENDING);
        bookingTime.setValue(LocalDateTime.now());

        // MUUTOS: manuaaliset bindaukset – referenceNumber jätetään pois
        binder.forField(bookerName)
                .asRequired("Varaajan nimi on pakollinen")
                .bind(Booking::getBookerName, Booking::setBookerName);

        binder.forField(bookerEmail)
                .asRequired("Sähköposti on pakollinen")
                .bind(Booking::getBookerEmail, Booking::setBookerEmail);

        binder.forField(numberOfSeats)
                .asRequired("Paikkoja on pakollinen")
                .bind(Booking::getNumberOfSeats, Booking::setNumberOfSeats);

        binder.forField(bookingTime)
                .bind(Booking::getBookingTime, Booking::setBookingTime);

        binder.forField(status)
                .asRequired("Status on pakollinen")
                .bind(Booking::getStatus, Booking::setStatus);

        binder.forField(event)
                .bind(Booking::getEvent, Booking::setEvent);

        // referenceNumber EI bindattu – generoidaan BookingService:ssä

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(e -> validateAndSave());
        delete.addClickListener(e ->
                fireEvent(new DeleteEvent(this, binder.getBean())));
        cancel.addClickListener(e ->
                fireEvent(new CancelEvent(this)));

        HorizontalLayout buttons =
                new HorizontalLayout(save, delete, cancel);

        add(bookerName, bookerEmail, numberOfSeats, bookingTime,
                status, referenceNumber, event, buttons);

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );
        setColspan(buttons, 2);
    }

    public void setBooking(Booking booking) {
        binder.setBean(booking);
        boolean isNew = booking == null || booking.getId() == null;
        delete.setVisible(!isNew);

        // Näytä viitenumero jos olemassa
        if (booking != null
                && booking.getReferenceNumber() != null
                && !booking.getReferenceNumber().isBlank()) {
            referenceNumber.setValue(booking.getReferenceNumber());
        } else {
            referenceNumber.setValue("");
            referenceNumber.setPlaceholder("Generoidaan automaattisesti");
        }

        // Oletusarvot uudelle varaukselle
        if (isNew) {
            status.setValue(BookingStatus.PENDING);
            bookingTime.setValue(LocalDateTime.now());
        }
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    // ---- Tapahtumat ----

    public static abstract class BookingFormEvent
            extends ComponentEvent<BookingForm> {
        private final Booking booking;

        protected BookingFormEvent(BookingForm source, Booking booking) {
            super(source, false);
            this.booking = booking;
        }

        public Booking getBooking() { return booking; }
    }

    public static class SaveEvent extends BookingFormEvent {
        SaveEvent(BookingForm source, Booking booking) {
            super(source, booking);
        }
    }

    public static class DeleteEvent extends BookingFormEvent {
        DeleteEvent(BookingForm source, Booking booking) {
            super(source, booking);
        }
    }

    public static class CancelEvent extends BookingFormEvent {
        CancelEvent(BookingForm source) { super(source, null); }
    }

    public Registration addSaveListener(
            ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    public Registration addDeleteListener(
            ComponentEventListener<DeleteEvent> listener) {
        return addListener(DeleteEvent.class, listener);
    }

    public Registration addCancelListener(
            ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}