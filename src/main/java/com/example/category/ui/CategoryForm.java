package com.example.category.ui;

import com.example.category.Category;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

public class CategoryForm extends FormLayout {

    private final TextField name = new TextField("Nimi");
    private final TextArea description = new TextArea("Kuvaus");
    private final TextField colorCode = new TextField("Värikoodi");

    private final Button save = new Button("Tallenna");
    private final Button delete = new Button("Poista");
    private final Button cancel = new Button("Peruuta");

    private final Binder<Category> binder =
            new BeanValidationBinder<>(Category.class);

    public CategoryForm() {
        addClassName("category-form");

        colorCode.setPlaceholder("#FF5733");
        colorCode.setHelperText("Muoto: #RRGGBB");
        description.setMinHeight("80px");

        binder.bindInstanceFields(this);

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

        add(name, colorCode, description, buttons);

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );
        setColspan(description, 2);
        setColspan(buttons, 2);
    }

    public void setCategory(Category category) {
        binder.setBean(category);
        boolean isNew = category == null || category.getId() == null;
        delete.setVisible(!isNew);
    }

    private void validateAndSave() {
        if (binder.isValid()) {
            fireEvent(new SaveEvent(this, binder.getBean()));
        }
    }

    // ---- Tapahtumat ----

    public static abstract class CategoryFormEvent
            extends ComponentEvent<CategoryForm> {
        private final Category category;

        protected CategoryFormEvent(CategoryForm source, Category category) {
            super(source, false);
            this.category = category;
        }

        public Category getCategory() { return category; }
    }

    public static class SaveEvent extends CategoryFormEvent {
        SaveEvent(CategoryForm source, Category category) {
            super(source, category);
        }
    }

    public static class DeleteEvent extends CategoryFormEvent {
        DeleteEvent(CategoryForm source, Category category) {
            super(source, category);
        }
    }

    public static class CancelEvent extends CategoryFormEvent {
        CancelEvent(CategoryForm source) { super(source, null); }
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