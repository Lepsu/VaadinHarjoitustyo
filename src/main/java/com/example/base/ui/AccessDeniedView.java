package com.example.base.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Access Denied")
@AnonymousAllowed
@Route(value = "access-denied")
public class AccessDeniedView extends Main implements BeforeEnterObserver {

    private final Paragraph message = new Paragraph();

    public AccessDeniedView() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(com.vaadin.flow.component.orderedlayout
                .FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(com.vaadin.flow.component
                .orderedlayout.FlexComponent.JustifyContentMode.CENTER);
        layout.setSizeFull();

        // Lumo Utility -luokat
        layout.addClassNames("p-xl", "text-center");

        var icon = VaadinIcon.BAN.create();
        icon.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("width", "64px")
                .set("height", "64px");

        H2 title = new H2("Pääsy estetty");
        title.getStyle().set("color", "var(--lumo-error-color)");

        message.addClassName("text-secondary");

        Button homeBtn = new Button("Takaisin etusivulle",
                VaadinIcon.HOME.create());
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeBtn.addClickListener(e ->
                homeBtn.getUI().ifPresent(ui -> ui.navigate("")));

        layout.add(icon, title, message, homeBtn);
        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Näytetään mikä sivu yritettiin avata
        String path = event.getLocation().getPath();
        message.setText(
                "Sinulla ei ole oikeuksia sivulle: /" + path +
                        ". Ota yhteyttä ylläpitäjään jos tarvitset pääsyn.");
    }
}