package com.example.admin.ui;

import com.example.user.AppUser;
import com.example.user.AppUserService;
import com.example.user.Role;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.example.base.PushNotificationService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import java.util.function.Consumer;

import java.util.Set;

@PageTitle("Admin")
@RolesAllowed("ADMIN")
@Route(value = "admin")
@Menu(title = "Admin", order = 7)
public class AdminView extends Main {

    private final AppUserService userService;
    private final AuthenticationContext authContext;
    private final PushNotificationService pushService;
    private Consumer<String> pushListener;

    private final Grid<AppUser> grid = new Grid<>(AppUser.class, false);

    public AdminView(AppUserService userService,
                     AuthenticationContext authContext, PushNotificationService pushService) {
        this.userService = userService;
        this.authContext = authContext;
        this.pushService = pushService;

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        layout.add(
                new H2("Ylläpito – Käyttäjähallinta"),
                createStatsRow(),
                new H3("Käyttäjät"),
                createToolbar(),
                createGrid()
        );

        add(layout);
        refreshGrid();
    }

    // ---- Tilastorivit ----
    private HorizontalLayout createStatsRow() {
        long total = userService.findAll().size();
        long admins = userService.findAll().stream()
                .filter(u -> u.getRoles().contains(Role.ADMIN))
                .count();
        long active = userService.findAll().stream()
                .filter(AppUser::isEnabled)
                .count();

        HorizontalLayout row = new HorizontalLayout(
                createStatCard("Käyttäjiä yhteensä",
                        String.valueOf(total), "badge"),
                createStatCard("Admineja",
                        String.valueOf(admins), "badge contrast"),
                createStatCard("Aktiivisia",
                        String.valueOf(active), "badge success")
        );
        row.setWidthFull();
        return row;
    }

    private VerticalLayout createStatCard(
            String label, String value, String theme) {
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "bold");

        Span labelSpan = new Span(label);
        labelSpan.getElement().getThemeList().add(theme);

        VerticalLayout card = new VerticalLayout(valueSpan, labelSpan);
        card.addClassNames("p-m", "shadow-xs");
        card.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("min-width", "150px");
        card.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.CENTER);
        return card;
    }

    // ---- Toolbar ----
    private HorizontalLayout createToolbar() {
        Button refreshBtn = new Button("Päivitä",
                VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(refreshBtn);
        toolbar.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.BASELINE);
        return toolbar;
    }

    // ---- Grid ----
    private Grid<AppUser> createGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.setMinHeight("400px");

        grid.addColumn(AppUser::getUsername)
                .setHeader("Käyttäjänimi")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(AppUser::getFirstName)
                .setHeader("Etunimi")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(AppUser::getLastName)
                .setHeader("Sukunimi")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(AppUser::getEmail)
                .setHeader("Sähköposti")
                .setFlexGrow(2);

        // Roolit badgeina
        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout badges = new HorizontalLayout();
            badges.setSpacing(true);
            user.getRoles().forEach(role -> {
                Span badge = new Span(role.name());
                String theme = "badge " + switch (role) {
                    case ADMIN -> "error";
                    case SUPER -> "contrast";
                    case USER -> "success";
                };
                badge.getElement().getThemeList().add(theme);
                badges.add(badge);
            });
            return badges;
        })).setHeader("Roolit").setFlexGrow(1);

        // Tila
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span status = new Span(
                    user.isEnabled() ? "Aktiivinen" : "Estetty");
            status.getElement().getThemeList().add(
                    "badge " + (user.isEnabled() ? "success" : "error"));
            return status;
        })).setHeader("Tila").setFlexGrow(0);

        // Toiminnot
        grid.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout actions = new HorizontalLayout();

            // Roolin vaihto
            Select<Role> roleSelect = new Select<>();
            roleSelect.setItems(Role.values());
            roleSelect.setPlaceholder("Lisää rooli");
            roleSelect.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    addRoleToUser(user, e.getValue());
                    roleSelect.clear();
                }
            });

            // Aktivoi/estä
            Button toggleBtn = new Button(
                    user.isEnabled() ? "Estä" : "Aktivoi");
            toggleBtn.addThemeVariants(
                    user.isEnabled()
                            ? ButtonVariant.LUMO_ERROR
                            : ButtonVariant.LUMO_SUCCESS,
                    ButtonVariant.LUMO_SMALL
            );
            toggleBtn.addClickListener(e ->
                    toggleUserEnabled(user));

            // Poista
            Button deleteBtn = new Button(
                    VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON
            );
            deleteBtn.addClickListener(e ->
                    confirmDelete(user));

            // Ei voi poistaa itseään
            String currentUsername = authContext
                    .getPrincipalName().orElse("");
            if (user.getUsername().equals(currentUsername)) {
                deleteBtn.setEnabled(false);
                toggleBtn.setEnabled(false);
            }

            actions.add(roleSelect, toggleBtn, deleteBtn);
            actions.setAlignItems(
                    com.vaadin.flow.component.orderedlayout
                            .FlexComponent.Alignment.CENTER);
            return actions;
        })).setHeader("Toiminnot").setFlexGrow(2).setAutoWidth(true);

        return grid;
    }

    // ---- Toiminnot ----

    private void addRoleToUser(AppUser user, Role role) {
        Set<Role> roles = user.getRoles();
        roles.add(role);
        user.setRoles(roles);
        userService.save(user);
        refreshGrid();
        showNotification("Rooli lisätty: " + role.name(), true);
    }

    private void toggleUserEnabled(AppUser user) {
        user.setEnabled(!user.isEnabled());
        userService.save(user);
        refreshGrid();
        showNotification(
                user.isEnabled()
                        ? "Käyttäjä aktivoitu"
                        : "Käyttäjä estetty",
                user.isEnabled()
        );
    }

    private void confirmDelete(AppUser user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Poista käyttäjä");
        dialog.setText("Haluatko varmasti poistaa käyttäjän "
                + user.getUsername() + "?");
        dialog.setCancelable(true);
        dialog.setCancelText("Peruuta");
        dialog.setConfirmText("Poista");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            userService.delete(user);
            refreshGrid();
            showNotification("Käyttäjä poistettu", false);
        });
        dialog.open();
    }

    private void refreshGrid() {
        grid.setItems(userService.findAll());
    }

    private void showNotification(String msg, boolean success) {
        Notification n = Notification.show(msg, 3000,
                Notification.Position.BOTTOM_START);
        n.addThemeVariants(success
                ? NotificationVariant.LUMO_SUCCESS
                : NotificationVariant.LUMO_ERROR);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        pushListener = message -> {
            ui.access(() -> {
                refreshGrid();
                Notification.show(message, 3000,
                        Notification.Position.BOTTOM_START);
            });
        };
        pushService.register(pushListener);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        pushService.unregister(pushListener);
        pushListener = null;
    }
}