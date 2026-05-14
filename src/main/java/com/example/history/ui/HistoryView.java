package com.example.history.ui;

import com.example.event.Event;
import com.example.event.EventService;
import com.example.history.HistoryEntry;
import com.example.history.HistoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

import java.util.List;

@PageTitle("History")
@PermitAll
@Route(value = "history")
@Menu(title = "History", order = 8)
public class HistoryView extends Main {

    private final EventService eventService;
    private final HistoryService historyService;

    private final ComboBox<Event> eventSelector =
            new ComboBox<>("Valitse tapahtuma");
    private final Grid<HistoryEntry> grid =
            new Grid<>(HistoryEntry.class, false);

    public HistoryView(EventService eventService,
                       HistoryService historyService) {
        this.eventService = eventService;
        this.historyService = historyService;

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        layout.add(
                new H2("Muutoshistoria"),
                createSelector(),
                new H3("Muutokset"),
                createGrid()
        );

        add(layout);
    }

    private HorizontalLayout createSelector() {
        eventSelector.setItems(eventService.findAll());
        eventSelector.setItemLabelGenerator(Event::getName);
        eventSelector.setPlaceholder("-- Valitse tapahtuma --");
        eventSelector.setClearButtonVisible(true);
        eventSelector.setWidth("300px");

        Button loadBtn = new Button("Näytä historia",
                VaadinIcon.CLOCK.create());
        loadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loadBtn.addClickListener(e -> loadHistory());

        HorizontalLayout row =
                new HorizontalLayout(eventSelector, loadBtn);
        row.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.BASELINE);
        return row;
    }

    private Grid<HistoryEntry> createGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.setMinHeight("400px");

        grid.addColumn(HistoryEntry::getRevision)
                .setHeader("Revisio")
                .setSortable(true)
                .setFlexGrow(0);

        grid.addColumn(HistoryEntry::getTimestamp)
                .setHeader("Aika")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(HistoryEntry::getOperation)
                .setHeader("Operaatio")
                .setFlexGrow(0);

        grid.addColumn(HistoryEntry::getEventName)
                .setHeader("Tapahtuman nimi")
                .setFlexGrow(2);

        grid.addColumn(HistoryEntry::getDescription)
                .setHeader("Kuvaus")
                .setFlexGrow(2);

        grid.addColumn(HistoryEntry::getModifiedBy)
                .setHeader("Muokkaaja")
                .setFlexGrow(1);

        return grid;
    }

    private void loadHistory() {
        if (eventSelector.getValue() == null) {
            Notification n = Notification.show(
                    "Valitse ensin tapahtuma", 3000,
                    Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }

        try {
            List<HistoryEntry> entries = historyService
                    .getEventHistory(eventSelector.getValue().getId());
            grid.setItems(entries);

            if (entries.isEmpty()) {
                Notification.show(
                        "Ei historiatietoja tälle tapahtumalle",
                        2000, Notification.Position.BOTTOM_START);
            }
        } catch (Exception e) {
            Notification n = Notification.show(
                    "Virhe historian latauksessa: " + e.getMessage(),
                    4000, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}