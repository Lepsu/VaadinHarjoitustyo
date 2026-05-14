package com.example.search.ui;

import com.example.event.Event;
import com.example.search.EventSearchService;
import com.example.venue.Venue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.component.select.Select;
import java.util.Locale;

import java.time.format.DateTimeFormatter;
import java.util.List;

@StyleSheet("search-view.css")
@PageTitle("Search")
@PermitAll
@Route(value = "search")
@Menu(title = "Search", order = 5)
public class SearchView extends VerticalLayout implements LocaleChangeObserver{

    private final EventSearchService searchService;
    private final Select<Locale> languageSelect = new Select<>();
    private final H2 title = new H2();

    // Hakukentät
    private final TextField nameOrDesc = new TextField("Nimi tai kuvaus");
    private final DatePicker dateFrom = new DatePicker("Alkaa aikaisintaan");
    private final DatePicker dateTo = new DatePicker("Alkaa viimeistään");
    private final TextField categoryName = new TextField("Kategoria");
    private final TextField venueSearch = new TextField("Tapahtumapaikka tai kaupunki");

    private final Button searchBtn = new Button("Hae",
            VaadinIcon.SEARCH.create());
    private final Button clearBtn = new Button("Tyhjennä",
            VaadinIcon.CLOSE.create());

    private final Grid<Event> grid = new Grid<>(Event.class, false);
    private final Span resultCount = new Span();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public SearchView(EventSearchService searchService) {
        this.searchService = searchService;

        addClassName("search-view");
        setSizeFull();
        setPadding(true);

        add(
                createLanguageSelector(),
                title,
                createSearchForm(),
                new Hr(),
                resultCount,
                createGrid()
        );

        // Aseta otsikko lokalisoinnilla
        title.setText(getTranslation("search.title"));
    }

    private HorizontalLayout createLanguageSelector() {
        languageSelect.setItems(
                new Locale("fi"),
                Locale.ENGLISH
        );
        languageSelect.setItemLabelGenerator(locale ->
                locale.getLanguage().equals("fi")
                        ? "🇫🇮 Suomi" : "🇬🇧 English");
        languageSelect.setValue(new Locale("fi"));
        languageSelect.setWidth("150px");
        languageSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                getUI().ifPresent(ui ->
                        ui.setLocale(e.getValue()));
            }
        });

        HorizontalLayout row =
                new HorizontalLayout(languageSelect);
        row.setJustifyContentMode(JustifyContentMode.END);
        row.setWidthFull();
        return row;
    }

    private FormLayout createSearchForm() {
        FormLayout form = new FormLayout();

        // Placeholder-tekstit selventämään hakulogiikkaa
        nameOrDesc.setPlaceholder("Hakee nimestä TAI kuvauksesta");
        nameOrDesc.setClearButtonVisible(true);
        nameOrDesc.setPrefixComponent(VaadinIcon.SEARCH.create());

        categoryName.setPlaceholder("esim. Musiikki");
        categoryName.setClearButtonVisible(true);

        venueSearch.setPlaceholder("esim. Helsinki tai Hartwall");
        venueSearch.setClearButtonVisible(true);

        // Päivämäärä validointi
        dateFrom.addValueChangeListener(e -> {
            if (e.getValue() != null && dateTo.getValue() != null
                    && e.getValue().isAfter(dateTo.getValue())) {
                dateTo.setValue(null);
            }
        });

        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> performSearch());

        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> clearSearch());

        HorizontalLayout buttons =
                new HorizontalLayout(searchBtn, clearBtn);
        buttons.setAlignItems(Alignment.BASELINE);

        form.add(
                nameOrDesc, venueSearch,
                dateFrom, dateTo,
                categoryName, buttons
        );

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        return form;
    }

    private Grid<Event> createGrid() {
        grid.setSizeFull();
        grid.setMinHeight("400px");

        grid.addColumn(Event::getName)
                .setHeader("Tapahtuma")
                .setSortable(true)
                .setFlexGrow(2);

        grid.addColumn(event -> {
            Venue v = event.getVenue();
            return v != null ? v.getName() + ", " + v.getCity() : "-";
        }).setHeader("Paikka").setFlexGrow(1);

        grid.addColumn(event ->
                event.getStartTime() != null
                        ? event.getStartTime().format(FORMATTER) : "-"
        ).setHeader("Alkaa").setSortable(true).setFlexGrow(1);

        grid.addColumn(event ->
                event.getPrice() != null ? "€ " + event.getPrice() : "-"
        ).setHeader("Hinta").setFlexGrow(0);

        // Kategoriat badgeina
        grid.addColumn(new ComponentRenderer<>(event -> {
            HorizontalLayout badges = new HorizontalLayout();
            event.getCategories().forEach(cat -> {
                Span badge = new Span(cat.getName());
                badge.getElement().getThemeList().add("badge");
                if (cat.getColorCode() != null) {
                    badge.getStyle()
                            .set("background-color", cat.getColorCode())
                            .set("color", "white");
                }
                badges.add(badge);
            });
            return badges;
        })).setHeader("Kategoriat").setFlexGrow(1);

        grid.addColumn(Event::getDescription)
                .setHeader("Kuvaus")
                .setFlexGrow(2);

        return grid;
    }

    private void performSearch() {
        String nameDescVal = nameOrDesc.getValue();
        String categoryVal = categoryName.getValue();
        String venueVal = venueSearch.getValue();

        // Jos kaikki tyhjä, näytetään info
        if ((nameDescVal == null || nameDescVal.isBlank())
                && dateFrom.getValue() == null
                && dateTo.getValue() == null
                && (categoryVal == null || categoryVal.isBlank())
                && (venueVal == null || venueVal.isBlank())) {
            Notification n = Notification.show(
                    "Anna vähintään yksi hakuehto", 3000,
                    Notification.Position.MIDDLE);
            n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            return;
        }

        List<Event> results = searchService.search(
                nameDescVal,
                dateFrom.getValue(),
                dateTo.getValue(),
                categoryVal,
                venueVal
        );

        grid.setItems(results);
        updateResultCount(results.size());
    }

    private void clearSearch() {
        nameOrDesc.clear();
        dateFrom.clear();
        dateTo.clear();
        categoryName.clear();
        venueSearch.clear();
        grid.setItems(List.of());
        resultCount.setText("");
    }

    private void updateResultCount(int count) {
        resultCount.setText(
                count == 0 ? "Ei hakutuloksia"
                        : "Löytyi " + count + " tapahtumaa"
        );
        resultCount.getElement().getThemeList().clear();
        resultCount.getElement().getThemeList().add(
                count == 0 ? "badge error" : "badge success"
        );
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        title.setText(getTranslation("search.title"));
        nameOrDesc.setLabel(getTranslation("search.nameOrDesc"));
        dateFrom.setLabel(getTranslation("search.dateFrom"));
        dateTo.setLabel(getTranslation("search.dateTo"));
        categoryName.setLabel(getTranslation("search.category"));
        venueSearch.setLabel(
                getTranslation("search.venue"));
        searchBtn.setText(getTranslation("search.button"));
        clearBtn.setText(getTranslation("search.clear"));
    }
}