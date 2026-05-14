package com.example.examplefeature.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Venues")
@PermitAll
@Route(value = "venues")
@Menu(title = "Venues")
public class Venues extends Main {

    public Venues() {
        add(new H1("This is Venues"));
    }
}
