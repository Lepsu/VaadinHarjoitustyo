package com.example.user.ui;

import com.example.user.AppUser;
import com.example.user.AppUserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.crypto.password.PasswordEncoder;

@PageTitle("Profile")
@PermitAll
@Route(value = "profile")
@Menu(title = "Profile", order = 6)
public class ProfileView extends Main {

    private final AppUserRepository userRepository;
    private final AuthenticationContext authContext;
    private final PasswordEncoder passwordEncoder;

    private final TextField firstName = new TextField("Etunimi");
    private final TextField lastName = new TextField("Sukunimi");
    private final EmailField email = new EmailField("Sähköposti");
    private final TextField username = new TextField("Käyttäjänimi");

    private final PasswordField currentPassword =
            new PasswordField("Nykyinen salasana");
    private final PasswordField newPassword =
            new PasswordField("Uusi salasana");
    private final PasswordField confirmNewPassword =
            new PasswordField("Vahvista uusi salasana");

    private AppUser currentUser;

    public ProfileView(AppUserRepository userRepository,
                       AuthenticationContext authContext,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authContext = authContext;
        this.passwordEncoder = passwordEncoder;

        VerticalLayout layout = new VerticalLayout();
        layout.setMaxWidth("600px");
        layout.getStyle().set("margin", "0 auto");

        loadCurrentUser();

        layout.add(
                new H2("Profiili"),
                createInfoSection(),
                createProfileForm(),
                createPasswordSection()
        );

        add(layout);
    }

    private void loadCurrentUser() {
        String name = authContext.getPrincipalName().orElse("");
        currentUser = userRepository.findByUsername(name).orElse(null);
        if (currentUser != null) {
            firstName.setValue(
                    nvl(currentUser.getFirstName()));
            lastName.setValue(
                    nvl(currentUser.getLastName()));
            email.setValue(
                    nvl(currentUser.getEmail()));
            username.setValue(
                    nvl(currentUser.getUsername()));
            username.setReadOnly(true);
        }
    }

    private VerticalLayout createInfoSection() {
        if (currentUser == null) return new VerticalLayout();

        Span rolesSpan = new Span("Roolit: " +
                currentUser.getRoles().toString());
        rolesSpan.addClassNames("text-secondary", "text-s");

        VerticalLayout info = new VerticalLayout(rolesSpan);
        info.setPadding(false);
        info.setSpacing(false);
        return info;
    }

    private VerticalLayout createProfileForm() {
        FormLayout form = new FormLayout();
        form.add(firstName, lastName, username, email);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        Button saveBtn = new Button("Tallenna tiedot");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> saveProfile());

        VerticalLayout section = new VerticalLayout(
                new H3("Perustiedot"), form, saveBtn);
        section.setPadding(false);
        return section;
    }

    private VerticalLayout createPasswordSection() {
        newPassword.setHelperText("Vähintään 6 merkkiä");

        FormLayout form = new FormLayout();
        form.add(currentPassword, newPassword, confirmNewPassword);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        Button changeBtn = new Button("Vaihda salasana");
        changeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changeBtn.addClickListener(e -> changePassword());

        VerticalLayout section = new VerticalLayout(
                new H3("Vaihda salasana"), form, changeBtn);
        section.setPadding(false);
        return section;
    }

    private void saveProfile() {
        if (currentUser == null) return;

        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty()) {
            showNotification("Täytä kaikki kentät", false);
            return;
        }

        currentUser.setFirstName(firstName.getValue());
        currentUser.setLastName(lastName.getValue());
        currentUser.setEmail(email.getValue());
        userRepository.save(currentUser);

        showNotification("Tiedot tallennettu!", true);
    }

    private void changePassword() {
        if (currentUser == null) return;

        if (currentPassword.isEmpty() || newPassword.isEmpty()
                || confirmNewPassword.isEmpty()) {
            showNotification("Täytä kaikki salasanakentät", false);
            return;
        }

        if (!passwordEncoder.matches(
                currentPassword.getValue(),
                currentUser.getPasswordHash())) {
            showNotification("Nykyinen salasana on väärä", false);
            return;
        }

        if (newPassword.getValue().length() < 6) {
            showNotification(
                    "Uuden salasanan oltava vähintään 6 merkkiä", false);
            return;
        }

        if (!newPassword.getValue()
                .equals(confirmNewPassword.getValue())) {
            showNotification("Uudet salasanat eivät täsmää", false);
            return;
        }

        currentUser.setPasswordHash(
                passwordEncoder.encode(newPassword.getValue()));
        userRepository.save(currentUser);

        currentPassword.clear();
        newPassword.clear();
        confirmNewPassword.clear();

        showNotification("Salasana vaihdettu!", true);
    }

    private void showNotification(String msg, boolean success) {
        Notification n = Notification.show(msg, 3000,
                Notification.Position.BOTTOM_START);
        n.addThemeVariants(success
                ? NotificationVariant.LUMO_SUCCESS
                : NotificationVariant.LUMO_ERROR);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}