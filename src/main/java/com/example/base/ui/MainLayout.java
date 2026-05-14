package com.example.base.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

@Layout
@AnonymousAllowed
public final class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        setPrimarySection(Section.DRAWER);
        addToNavbar(createNavbarContent());
        addToDrawer(
                createDrawerHeader(),
                createApplicationDrawer(),
                createApplicationFooter()
        );
    }

    // ---- Navbar (yläpalkki) ----
    private Component createNavbarContent() {
        DrawerToggle toggle = new DrawerToggle();

        H1 appTitle = new H1("EventApp");
        appTitle.addClassName("app-name");
        appTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        // 2b. getStyle().set() käyttö
        Span userInfo = new Span(getUsername());
        userInfo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-right", "var(--lumo-space-m)");

        Button logoutBtn = new Button("Kirjaudu ulos",
                VaadinIcon.SIGN_OUT.create());
        // 2c. addThemeVariants käyttö
        logoutBtn.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_SMALL
        );
        logoutBtn.addClickListener(e ->
                authContext.logout());

        HorizontalLayout right =
                new HorizontalLayout(userInfo, logoutBtn);
        right.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout navbar =
                new HorizontalLayout(toggle, appTitle, right);
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.expand(appTitle);
        // 2a. addClassName käyttö
        navbar.addClassName("app-header");

        return navbar;
    }

    // ---- Drawer header ----
    private Component createDrawerHeader() {
        Avatar appLogo = new Avatar("EA");
        appLogo.addClassName("app-logo");
        // 2c. addThemeVariants
        appLogo.addThemeVariants(AvatarVariant.LUMO_LARGE);
        appLogo.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white");

        Span appName = new Span("EventApp");
        appName.addClassName("app-name");
        // 2b. getStyle().set()
        appName.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        HorizontalLayout header =
                new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        return header;
    }

    // ---- Drawer navigaatio ----
    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);
        MenuConfiguration.getMenuEntries().forEach(entry ->
                nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            Component icon;
            if (menuEntry.icon().contains(".svg")) {
                icon = new SvgIcon(menuEntry.icon());
            } else {
                icon = new Icon(menuEntry.icon());
            }
            return new SideNavItem(
                    menuEntry.title(), menuEntry.path(), icon);
        }
        return new SideNavItem(menuEntry.title(), menuEntry.path());
    }

    // ---- Footer ----
    private Component createApplicationFooter() {
        Paragraph copyright = new Paragraph(
                "© 2025 EventApp | Tekijä: Etunimi Sukunimi");
        // 4. Lumo Utility -luokkien käyttö (viisi luokkaa)
        copyright.addClassNames(
                "text-secondary",   // TextColor
                "text-xs",          // FontSize
                "m-0"               // Margin
        );

        Anchor github = new Anchor(
                "https://github.com/sinun-repo", "GitHub");
        Anchor docs = new Anchor("#", "Dokumentaatio");

        HorizontalLayout links = new HorizontalLayout(github, docs);
        links.addClassNames(
                "justify-center",   // Display/justify
                "gap-m"             // Margin/gap
        );

        VerticalLayout footer = new VerticalLayout(copyright, links);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setSpacing(false);
        // 4. Lumo Utility – Padding, BoxShadow, BorderRadius
        footer.addClassNames(
                "app-footer",
                "p-s",              // Padding
                "shadow-xs"         // BoxShadow
        );

        return footer;
    }

    private String getUsername() {
        return authContext.getPrincipalName()
                .orElse("Vieras");
    }
}