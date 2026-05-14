package com.example.venue.ui;

import com.example.venue.Venue;
import com.example.venue.VenueService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Venues")
@PermitAll
@Route(value = "venues")
@Menu(title = "Venues", order = 2)
public class VenueListView extends VerticalLayout {

    private final VenueService venueService;

    private final Grid<Venue> grid = new Grid<>(Venue.class, false);
    private final TextField searchField = new TextField();
    private VenueForm form;

    public VenueListView(VenueService venueService) {
        this.venueService = venueService;

        addClassName("venue-list-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(createToolbar(), createContent());
        updateList();
        closeForm();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(Venue::getName)
                .setHeader("Nimi")
                .setSortable(true)
                .setFlexGrow(2);

        grid.addColumn(Venue::getAddress)
                .setHeader("Osoite")
                .setFlexGrow(2);

        grid.addColumn(Venue::getCity)
                .setHeader("Kaupunki")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(Venue::getCapacity)
                .setHeader("Kapasiteetti")
                .setSortable(true)
                .setFlexGrow(0);

        grid.addColumn(Venue::getContactEmail)
                .setHeader("Sähköposti")
                .setFlexGrow(2);

        grid.addColumn(Venue::getPhone)
                .setHeader("Puhelin")
                .setFlexGrow(1);

        grid.asSingleSelect().addValueChangeListener(e ->
                editVenue(e.getValue()));
    }

    private void configureForm() {
        form = new VenueForm();
        form.setWidth("400px");

        form.addSaveListener(this::saveVenue);
        form.addDeleteListener(this::deleteVenue);
        form.addCancelListener(e -> closeForm());
    }

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Hae paikkaa...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Uusi paikka",
                VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addVenue());

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
            grid.setItems(venueService.findAll());
        } else {
            grid.setItems(venueService.findAll().stream()
                    .filter(v -> v.getName().toLowerCase()
                            .contains(filter.toLowerCase())
                            || v.getCity().toLowerCase()
                            .contains(filter.toLowerCase()))
                    .toList());
        }
    }

    private void addVenue() {
        grid.asSingleSelect().clear();
        editVenue(new Venue());
    }

    private void editVenue(Venue venue) {
        if (venue == null) {
            closeForm();
        } else {
            form.setVenue(venue);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void saveVenue(VenueForm.SaveEvent event) {
        venueService.save(event.getVenue());
        updateList();
        closeForm();
        showNotification("Tapahtumapaikka tallennettu!", true);
    }

    private void deleteVenue(VenueForm.DeleteEvent event) {
        venueService.delete(event.getVenue());
        updateList();
        closeForm();
        showNotification("Tapahtumapaikka poistettu!", false);
    }

    private void closeForm() {
        form.setVenue(null);
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