package com.example.event.ui;

import com.example.category.Category;
import com.example.event.Event;
import com.example.venue.Venue;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import java.util.List;

public class EventForm extends FormLayout {

    private final TextField name = new TextField("Tapahtuman nimi");
    private final TextArea description = new TextArea("Kuvaus");
    private final DateTimePicker startTime = new DateTimePicker("Alkamisaika");
    private final DateTimePicker endTime = new DateTimePicker("Päättymisaika");
    private final IntegerField maxAttendees = new IntegerField("Maks. osallistujat");
    private final BigDecimalField price = new BigDecimalField("Hinta (€)");
    private final ComboBox<Venue> venue = new ComboBox<>();
    private final MultiSelectComboBox<Category> categories =
            new MultiSelectComboBox<>("Kategoriat");

    private final Button save = new Button("Tallenna");
    private final Button delete = new Button("Poista");
    private final Button cancel = new Button("Peruuta");

    private final Binder<Event> binder = new BeanValidationBinder<>(Event.class);

    public EventForm(List<Venue> venues, List<Category> allCategories) {
        addClassName("event-form");

        // Venue select
        venue.setLabel("Tapahtumapaikka");
        venue.setItems(venues);
        venue.setItemLabelGenerator(Venue::getName);
        venue.setPlaceholder("-- Valitse paikka --");
        venue.setClearButtonVisible(true);

        // Categories multi-select
        categories.setItems(allCategories);
        categories.setItemLabelGenerator(Category::getName);

        // Field settings
        description.setMinHeight("100px");
        maxAttendees.setMin(1);
        maxAttendees.setMax(10000);
        price.setPrefixComponent(new com.vaadin.flow.component.html.Span("€"));

        // Binder
        binder.bindInstanceFields(this);

        // Buttons
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(e -> validateAndSave());
        delete.addClickListener(e -> fireEvent(new DeleteEvent(this, binder.getBean())));
        cancel.addClickListener(e -> fireEvent(new CancelEvent(this)));

        HorizontalLayout buttons = new HorizontalLayout(save, delete, cancel);

        add(name, description, startTime, endTime,
                maxAttendees, price, venue, categories, buttons);

        // Lomake 2-sarakkeiseksi
        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );
        setColspan(description, 2);
        setColspan(buttons, 2);
    }

    public void setEvent(Event event) {
        binder.setBean(event);
        boolean isNew = event == null || event.getId() == null;
        delete.setVisible(!isNew);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    // ---- Tapahtumat (Events) ----

    public static abstract class EventFormEvent
            extends ComponentEvent<EventForm> {
        private final Event event;

        protected EventFormEvent(EventForm source, Event event) {
            super(source, false);
            this.event = event;
        }

        public Event getEvent() { return event; }
    }

    public static class SaveEvent extends EventFormEvent {
        SaveEvent(EventForm source, Event event) {
            super(source, event);
        }
    }

    public static class DeleteEvent extends EventFormEvent {
        DeleteEvent(EventForm source, Event event) {
            super(source, event);
        }
    }

    public static class CancelEvent extends EventFormEvent {
        CancelEvent(EventForm source) {
            super(source, null);
        }
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