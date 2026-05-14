package com.example.category.ui;

import com.example.category.Category;
import com.example.category.CategoryService;
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

@PageTitle("Categories")
@PermitAll
@Route(value = "categories")
@Menu(title = "Categories", order = 4)
public class CategoryListView extends VerticalLayout {

    private final CategoryService categoryService;

    private final Grid<Category> grid = new Grid<>(Category.class, false);
    private final TextField searchField = new TextField();
    private CategoryForm form;

    public CategoryListView(CategoryService categoryService) {
        this.categoryService = categoryService;

        addClassName("category-list-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(createToolbar(), createContent());
        updateList();
        closeForm();
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Värikoodi badge-renderöinnillä
        grid.addColumn(new ComponentRenderer<>(cat -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            if (cat.getColorCode() != null
                    && !cat.getColorCode().isBlank()) {
                Span dot = new Span();
                dot.getStyle()
                        .set("background-color", cat.getColorCode())
                        .set("width", "16px")
                        .set("height", "16px")
                        .set("border-radius", "50%")
                        .set("display", "inline-block");
                layout.add(dot);
            }
            layout.add(new Span(cat.getName()));
            return layout;
        })).setHeader("Nimi").setSortable(true).setFlexGrow(1);

        grid.addColumn(Category::getDescription)
                .setHeader("Kuvaus")
                .setFlexGrow(3);

        grid.addColumn(Category::getColorCode)
                .setHeader("Värikoodi")
                .setFlexGrow(0);

        // M:N relaatio – tapahtumien määrä
        grid.addColumn(cat -> cat.getEvents().size() + " tapahtumaa")
                .setHeader("Tapahtumat")
                .setFlexGrow(0);

        grid.asSingleSelect().addValueChangeListener(e ->
                editCategory(e.getValue()));
    }

    private void configureForm() {
        form = new CategoryForm();
        form.setWidth("400px");

        form.addSaveListener(this::saveCategory);
        form.addDeleteListener(this::deleteCategory);
        form.addCancelListener(e -> closeForm());
    }

    private HorizontalLayout createToolbar() {
        searchField.setPlaceholder("Hae kategoriaa...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        Button addButton = new Button("Uusi kategoria",
                VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> addCategory());

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
            grid.setItems(categoryService.findAll());
        } else {
            grid.setItems(categoryService.findAll().stream()
                    .filter(c -> c.getName().toLowerCase()
                            .contains(filter.toLowerCase()))
                    .toList());
        }
    }

    private void addCategory() {
        grid.asSingleSelect().clear();
        editCategory(new Category());
    }

    private void editCategory(Category category) {
        if (category == null) {
            closeForm();
        } else {
            form.setCategory(category);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void saveCategory(CategoryForm.SaveEvent event) {
        categoryService.save(event.getCategory());
        updateList();
        closeForm();
        showNotification("Kategoria tallennettu!", true);
    }

    private void deleteCategory(CategoryForm.DeleteEvent event) {
        categoryService.delete(event.getCategory());
        updateList();
        closeForm();
        showNotification("Kategoria poistettu!", false);
    }

    private void closeForm() {
        form.setCategory(null);
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