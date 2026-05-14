package com.example.examplefeature.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("History")
@PermitAll
@Route(value = "history")
@Menu(title = "History")
public class History extends Main {

    public History() {
        add(new H1("This is History"));
    }
}
