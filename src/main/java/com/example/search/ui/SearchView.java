package com.example.examplefeature.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;

@PageTitle("Search")
@PermitAll
@Route(value = "search")
@Menu(title = "Search")
public class Search extends Main {

    public Search() {
        add(new H1("This is Search"));
    }
}
