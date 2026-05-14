package com.example.examplefeature.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Categories")
@PermitAll
@Route(value = "categories")
@Menu(title = "Categories")
public class Categories extends Main {

    public Categories() {
        add(new H1("This is Categories"));
    }
}
