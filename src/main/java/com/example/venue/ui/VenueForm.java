package com.example.venue.ui;

import com.example.venue.Venue;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

public class VenueForm extends FormLayout {

    private final TextField name = new TextField("Nimi");
    private final TextField address = new TextField("Osoite");
    private final TextField city = new TextField("Kaupunki");
    private final IntegerField capacity = new IntegerField("Kapasiteetti");
    private final EmailField contactEmail = new EmailField("Sähköposti");
    private final TextField phone = new TextField("Puhelin");

    private final Button save = new Button("Tallenna");
    private final Button delete = new Button("Poista");
    private final Button cancel = new Button("Peruuta");

    private final Binder<Venue> binder = new BeanValidationBinder<>(Venue.class);

    public VenueForm() {
        addClassName("venue-form");

        capacity.setMin(1);
        capacity.setMax(100000);

        binder.bindInstanceFields(this);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(e -> validateAndSave());
        delete.addClickListener(e ->
                fireEvent(new DeleteEvent(this, binder.getBean())));
        cancel.addClickListener(e ->
                fireEvent(new CancelEvent(this)));

        HorizontalLayout buttons = new HorizontalLayout(save, delete, cancel);

        add(name, address, city, capacity, contactEmail, phone, buttons);

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );
        setColspan(buttons, 2);
    }

    public void setVenue(Venue venue) {
        binder.setBean(venue);
        boolean isNew = venue == null || venue.getId() == null;
        delete.setVisible(!isNew);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    // ---- Tapahtumat ----

    public static abstract class VenueFormEvent
            extends ComponentEvent<VenueForm> {
        private final Venue venue;

        protected VenueFormEvent(VenueForm source, Venue venue) {
            super(source, false);
            this.venue = venue;
        }

        public Venue getVenue() { return venue; }
    }

    public static class SaveEvent extends VenueFormEvent {
        SaveEvent(VenueForm source, Venue venue) { super(source, venue); }
    }

    public static class DeleteEvent extends VenueFormEvent {
        DeleteEvent(VenueForm source, Venue venue) { super(source, venue); }
    }

    public static class CancelEvent extends VenueFormEvent {
        CancelEvent(VenueForm source) { super(source, null); }
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