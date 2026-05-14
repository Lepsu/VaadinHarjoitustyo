package com.example.user.ui;

import com.example.user.AppUserService;
import com.example.user.Role;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Set;

@PageTitle("Register")
@AnonymousAllowed
@Route(value = "register")
public class RegisterView extends Main {

    private final AppUserService userService;

    private final TextField firstName = new TextField("Etunimi");
    private final TextField lastName = new TextField("Sukunimi");
    private final TextField username = new TextField("Käyttäjänimi");
    private final EmailField email = new EmailField("Sähköposti");
    private final PasswordField password = new PasswordField("Salasana");
    private final PasswordField confirmPassword =
            new PasswordField("Vahvista salasana");

    private final Button registerBtn = new Button("Rekisteröidy");
    private final Button cancelBtn = new Button("Peruuta");

    public RegisterView(AppUserService userService) {
        this.userService = userService;

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.CENTER);
        layout.setMaxWidth("500px");
        layout.getStyle().set("margin", "0 auto");

        H2 title = new H2("Luo uusi tili");

        FormLayout form = new FormLayout();
        form.setWidthFull();

        firstName.setRequired(true);
        lastName.setRequired(true);
        username.setRequired(true);
        username.setHelperText("Vähintään 3 merkkiä");
        email.setRequired(true);
        password.setRequired(true);
        password.setHelperText("Vähintään 6 merkkiä");
        confirmPassword.setRequired(true);

        form.add(firstName, lastName, username, email,
                password, confirmPassword);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );
        form.setColspan(username, 2);
        form.setColspan(email, 2);

        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();
        registerBtn.addClickListener(e -> handleRegister());

        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.setWidthFull();
        cancelBtn.addClickListener(e ->
                cancelBtn.getUI().ifPresent(ui -> ui.navigate("login")));

        Paragraph loginLink = new Paragraph("Onko sinulla jo tili? ");
        RouterLink link = new RouterLink("Kirjaudu sisään", LoginView.class);
        loginLink.add(link);

        layout.add(title, form, registerBtn, cancelBtn, loginLink);
        layout.setPadding(true);
        add(layout);
    }

    private void handleRegister() {
        // Validointi
        if (firstName.isEmpty() || lastName.isEmpty()
                || username.isEmpty() || email.isEmpty()
                || password.isEmpty()) {
            showError("Täytä kaikki pakolliset kentät");
            return;
        }

        if (username.getValue().length() < 3) {
            showError("Käyttäjänimen oltava vähintään 3 merkkiä");
            return;
        }

        if (password.getValue().length() < 6) {
            showError("Salasanan oltava vähintään 6 merkkiä");
            return;
        }

        if (!password.getValue().equals(confirmPassword.getValue())) {
            showError("Salasanat eivät täsmää");
            return;
        }

        if (userService.usernameExists(username.getValue())) {
            showError("Käyttäjänimi on jo käytössä");
            return;
        }

        if (userService.emailExists(email.getValue())) {
            showError("Sähköposti on jo käytössä");
            return;
        }

        // Luodaan käyttäjä
        userService.createUser(
                username.getValue(),
                email.getValue(),
                password.getValue(),
                Set.of(Role.USER),
                firstName.getValue(),
                lastName.getValue()
        );

        Notification n = Notification.show(
                "Tili luotu! Voit nyt kirjautua sisään.",
                4000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Ohjataan login-sivulle
        getUI().ifPresent(ui -> ui.navigate("login"));
    }

    private void showError(String message) {
        Notification n = Notification.show(message, 4000,
                Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}