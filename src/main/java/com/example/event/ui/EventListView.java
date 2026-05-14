package com.example.event.ui;

import com.example.category.CategoryService;
import com.example.event.CsvService;
import com.example.event.Event;
import com.example.event.EventService;
import com.example.venue.Venue;
import com.example.venue.VenueService;
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
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import java.time.format.DateTimeFormatter;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import java.io.ByteArrayInputStream;
import java.util.List;

@PageTitle("Events")
@PermitAll
@Route(value = "events")
@Menu(title = "Events", order = 1)
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;
    private final CsvService csvService;

    private final Grid<Event> grid = new Grid<>(Event.class, false);
    private final TextField searchField = new TextField();
    private EventForm form;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public EventListView(EventService eventService,
                         VenueService venueService,
                         CategoryService categoryService, CsvService csvService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.categoryService = categoryService;
        this.csvService = csvService;

        addClassName("event-list-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(createToolbar(), createContent());
        updateList();
        closeForm();
    }

    // ---- Grid ----

    private void configureGrid() {
        grid.addClassName("event-grid");
        grid.setSizeFull();

        // Sarakkeet
        grid.addColumn(Event::getName)
                .setHeader("Tapahtuma")
                .setSortable(true)
                .setFlexGrow(2);

        // 1:1 relaatio – Venue näkyy gridissä
        grid.addColumn(event -> {
            Venue v = event.getVenue();
            return v != null ? v.getName() + ", " + v.getCity() : "-";
        }).setHeader("Tapahtumapaikka").setSortable(true).setFlexGrow(1);

        grid.addColumn(event ->
                event.getStartTime() != null
                        ? event.getStartTime().format(FORMATTER) : "-"
        ).setHeader("Alkaa").setSortable(true).setFlexGrow(1);

        grid.addColumn(event ->
                event.getEndTime() != null
                        ? event.getEndTime().format(FORMATTER) : "-"
        ).setHeader("Päättyy").setFlexGrow(1);

        grid.addColumn(event ->
                event.getPrice() != null
                        ? "€ " + event.getPrice() : "-"
        ).setHeader("Hinta").setFlexGrow(0);

        grid.addColumn(Event::getMaxAttendees)
                .setHeader("Paikat")
                .setFlexGrow(0);

        // M:N relaatio – Kategoriat näkyy gridissä badgeina
        grid.addColumn(new ComponentRenderer<>(event -> {
            HorizontalLayout badges = new HorizontalLayout();
            badges.setSpacing(true);
            event.getCategories().forEach(cat -> {
                Span badge = new Span(cat.getName());
                badge.getElement().getThemeList().add("badge");
                if (cat.getColorCode() != null) {
                    badge.getStyle().set("background-color",
                            cat.getColorCode());
                    badge.getStyle().set("color", "white");
                }
                badges.add(badge);
            });
            return badges;
        })).setHeader("Kategoriat").setFlexGrow(1);

        // Klikkaus avaa lomakkeen
        grid.asSingleSelect().addValueChangeListener(e ->
                editEvent(e.getValue()));
    }

    // ---- Form ----

    private void configureForm() {
        form = new EventForm(
                venueService.findAll(),
                categoryService.findAll()
        );
        form.setWidth("400px");

        form.addSaveListener(this::saveEvent);
        form.addDeleteListener(this::deleteEvent);
        form.addCancelListener(e -> closeForm());
    }

    // ---- Toolbar ----

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Hae tapahtumaa...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Uusi tapahtuma",
                VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addEvent());

        // CSV-vientinappi
        Button exportBtn = new Button("Vie CSV",
                VaadinIcon.DOWNLOAD.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        exportBtn.addClickListener(e -> exportCsv());

        // CSV-tuontinappi
        Button importBtn = new Button("Tuo CSV",
                VaadinIcon.UPLOAD.create());
        importBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        importBtn.addClickListener(e -> openImportDialog());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, addButton, exportBtn, importBtn);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        toolbar.expand(searchField);
        return toolbar;
    }

    // ---- Layout ----

    private HorizontalLayout createContent() {
        HorizontalLayout content =
                new HorizontalLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setSizeFull();
        return content;
    }

    // ---- CRUD-operaatiot ----

    private void updateList() {
        String filter = searchField.getValue();
        if (filter == null || filter.isBlank()) {
            grid.setItems(eventService.findAll());
        } else {
            grid.setItems(eventService.findByName(filter));
        }
    }

    private void addEvent() {
        grid.asSingleSelect().clear();
        editEvent(new Event());
    }

    private void editEvent(Event event) {
        if (event == null) {
            closeForm();
        } else {
            form.setEvent(event);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void saveEvent(EventForm.SaveEvent event) {
        eventService.save(event.getEvent());
        updateList();
        closeForm();
        showNotification("Tapahtuma tallennettu!", true);
    }

    private void deleteEvent(EventForm.DeleteEvent event) {
        eventService.delete(event.getEvent());
        updateList();
        closeForm();
        showNotification("Tapahtuma poistettu!", false);
    }

    private void closeForm() {
        form.setEvent(null);
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

    // ---- CSV-vienti ----
    private void exportCsv() {
        List<Event> events = eventService.findAll();

        if (events.isEmpty()) {
            showNotification("Ei tapahtumia vietäväksi", false);
            return;
        }

        StreamResource resource = new StreamResource(
                "tapahtumat.csv",
                () -> new ByteArrayInputStream(
                        csvService.exportEventsToCsv(events))
        );

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement()
                .setAttribute("download", true);
        downloadLink.setId("csv-download-link");
        downloadLink.getStyle().set("display", "none");

        add(downloadLink);

        // Klikkaa linkkiä automaattisesti
        downloadLink.getElement().executeJs(
                "this.click(); "
                        + "setTimeout(() => this.remove(), 1000);");

        showNotification(
                "CSV-tiedosto ladataan (" + events.size()
                        + " tapahtumaa)", true);
    }

    // ---- CSV-tuonti ----
    private void openImportDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");

        H3 title = new H3("Tuo tapahtumat CSV:stä");
        Paragraph info = new Paragraph(
                "CSV-tiedostossa tulee olla sarakkeet: "
                        + "Nimi, Kuvaus, Alkamisaika, Päättymisaika, "
                        + "Maks. osallistujat, Hinta. "
                        + "Päivämäärämuoto: dd.MM.yyyy HH:mm");
        info.addClassName("text-secondary");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv", "text/csv");
        upload.setMaxFiles(1);
        upload.setDropLabel(
                new com.vaadin.flow.component.html.Span(
                        "Pudota CSV-tiedosto tähän"));

        Span resultSpan = new Span();

        upload.addSucceededListener(e -> {
            try {
                List<Event> imported = csvService
                        .importEventsFromCsv(
                                buffer.getInputStream());

                int count = 0;
                for (Event event : imported) {
                    if (event.getName() != null
                            && !event.getName().isBlank()) {
                        eventService.save(event);
                        count++;
                    }
                }

                int finalCount = count;
                resultSpan.setText(
                        "Tuotu " + finalCount + " tapahtumaa!");
                resultSpan.getElement()
                        .getThemeList().add("badge success");
                updateList();

            } catch (Exception ex) {
                resultSpan.setText(
                        "Virhe tuonnissa: " + ex.getMessage());
                resultSpan.getElement()
                        .getThemeList().add("badge error");
            }
        });

        Button closeBtn = new Button("Sulje",
                VaadinIcon.CLOSE.create());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClickListener(e -> dialog.close());

        VerticalLayout content = new VerticalLayout(
                title, info, upload, resultSpan, closeBtn);
        content.setAlignItems(Alignment.STRETCH);
        dialog.add(content);
        dialog.open();
    }
}