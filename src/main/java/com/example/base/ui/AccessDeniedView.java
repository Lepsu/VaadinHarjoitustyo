package com.example.examplefeature.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Access Denied")
@AnonymousAllowed
@Route(value = "access-denied")
public class AccessDenied extends Main {

    public AccessDenied() {
        add(new H1("This is Access Denied"));
    }
}
