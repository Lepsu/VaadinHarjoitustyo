package com.example.booking.ui;

import com.example.booking.Booking;
import com.example.booking.BookingService;
import com.example.booking.BookingStatus;
import com.example.event.EventService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

import java.time.format.DateTimeFormatter;

@PageTitle("Bookings")
@RolesAllowed({"USER", "SUPER", "ADMIN"})
@Route(value = "bookings")
@Menu(title = "Bookings", order = 3)
public class BookingListView extends VerticalLayout {

    private final BookingService bookingService;
    private final EventService eventService;

    private final Grid<Booking> grid = new Grid<>(Booking.class, false);
    private final TextField searchField = new TextField();
    private BookingForm form;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public BookingListView(BookingService bookingService,
                           EventService eventService) {
        this.bookingService = bookingService;
        this.eventService = eventService;

        addClassName("booking-list-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(createToolbar(), createContent());
        updateList();
        closeForm();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(Booking::getBookerName)
                .setHeader("Varaaja")
                .setSortable(true)
                .setFlexGrow(2);

        grid.addColumn(Booking::getBookerEmail)
                .setHeader("Sähköposti")
                .setFlexGrow(2);

        // 1:N relaatio – tapahtuma näkyy varauksessa
        grid.addColumn(b -> b.getEvent() != null
                        ? b.getEvent().getName() : "-")
                .setHeader("Tapahtuma")
                .setSortable(true)
                .setFlexGrow(2);

        grid.addColumn(Booking::getNumberOfSeats)
                .setHeader("Paikkoja")
                .setFlexGrow(0);

        grid.addColumn(b -> b.getBookingTime() != null
                        ? b.getBookingTime().format(FORMATTER) : "-")
                .setHeader("Varausaika")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(Booking::getReferenceNumber)
                .setHeader("Viitenumero")
                .setFlexGrow(1);

        // Status badge-renderöinnillä
        grid.addColumn(new ComponentRenderer<>(booking -> {
            Span badge = new Span();
            BookingStatus s = booking.getStatus();
            if (s == null) return badge;
            badge.setText(switch (s) {
                case PENDING -> "Odottaa";
                case CONFIRMED -> "Vahvistettu";
                case CANCELLED -> "Peruutettu";
            });
            String theme = "badge " + switch (s) {
                case PENDING -> "contrast";
                case CONFIRMED -> "success";
                case CANCELLED -> "error";
            };
            badge.getElement().getThemeList().add(theme);
            return badge;
        })).setHeader("Status").setFlexGrow(1);

        grid.asSingleSelect().addValueChangeListener(e ->
                editBooking(e.getValue()));
    }

    private void configureForm() {
        form = new BookingForm(eventService.findAll());
        form.setWidth("400px");

        form.addSaveListener(this::saveBooking);
        form.addDeleteListener(this::deleteBooking);
        form.addCancelListener(e -> closeForm());
    }

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Hae varaajaa...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Uusi varaus",
                VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addBooking());

        HorizontalLayout toolbar =
                new HorizontalLayout(searchField, addButton);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.expand(searchField);
        return toolbar;
    }

    private HorizontalLayout createContent() {
        HorizontalLayout content =
                new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setSizeFull();
        return content;
    }

    private void updateList() {
        String filter = searchField.getValue();
        if (filter == null || filter.isBlank()) {
            grid.setItems(bookingService.findAll());
        } else {
            grid.setItems(bookingService.findAll().stream()
                    .filter(b -> b.getBookerName().toLowerCase()
                            .contains(filter.toLowerCase())
                            || b.getBookerEmail().toLowerCase()
                            .contains(filter.toLowerCase()))
                    .toList());
        }
    }

    private void addBooking() {
        grid.asSingleSelect().clear();
        editBooking(new Booking());
    }

    private void editBooking(Booking booking) {
        if (booking == null) {
            closeForm();
        } else {
            form.setBooking(booking);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void saveBooking(BookingForm.SaveEvent event) {
        bookingService.save(event.getBooking());
        updateList();
        closeForm();
        showNotification("Varaus tallennettu!", true);
    }

    private void deleteBooking(BookingForm.DeleteEvent event) {
        bookingService.delete(event.getBooking());
        updateList();
        closeForm();
        showNotification("Varaus poistettu!", false);
    }

    private void closeForm() {
        form.setBooking(null);
        form.setVisible(false);
        removeClassName("editing");
    }

    private void showNotification(String message, boolean success) {
        Notification notification = Notification.show(message, 3000,
                Notification.Position.BOTTOM_START);
        notification.addThemeVariants(success
                ? NotificationVariant.LUMO_SUCCESS
                : NotificationVariant.LUMO_ERROR);
    }
}