package com.example.user.ui;

import com.example.base.EmailService;
import com.example.user.AppUser;
import com.example.user.AppUserRepository;
import com.example.user.PasswordResetToken;
import com.example.user.PasswordResetTokenRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@PageTitle("Unohtunut salasana")
@AnonymousAllowed
@Route(value = "forgot-password")
public class ForgotPasswordView extends Main {

    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    private final EmailField emailField =
            new EmailField("Sähköpostiosoite");
    private final Button sendBtn =
            new Button("Lähetä palautuslinkki");

    public ForgotPasswordView(
            AppUserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.CENTER);
        layout.setMaxWidth("400px");
        layout.getStyle().set("margin", "0 auto");

        emailField.setWidthFull();
        emailField.setRequired(true);

        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendBtn.setWidthFull();
        sendBtn.addClickListener(e -> handleReset());

        Button backBtn = new Button("Takaisin kirjautumiseen");
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.setWidthFull();
        backBtn.addClickListener(e ->
                backBtn.getUI().ifPresent(
                        ui -> ui.navigate("login")));

        layout.add(
                new H2("Unohtunut salasana"),
                new Paragraph(
                        "Syötä sähköpostiosoitteesi niin lähetämme "
                                + "sinulle salasanan palautuslinkin."),
                emailField,
                sendBtn,
                backBtn
        );

        add(layout);
    }

    private void handleReset() {
        if (emailField.isEmpty()) {
            showNotification(
                    "Syötä sähköpostiosoite", false);
            return;
        }

        Optional<AppUser> userOpt = userRepository
                .findByEmail(emailField.getValue());

        // Näytetään aina sama viesti
        // tietoturvan takia
        String successMsg =
                "Jos sähköposti löytyy järjestelmästä, "
                        + "lähetämme palautuslinkin pian.";

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();

            // Poista vanhat tokenit
            tokenRepository.deleteByUser(user);

            // Luo uusi token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken =
                    new PasswordResetToken(
                            token,
                            user,
                            LocalDateTime.now().plusHours(24)
                    );
            tokenRepository.save(resetToken);

            // Lähetä sähköposti
            String resetLink =
                    "http://localhost:8080/reset-password?token="
                            + token;
            emailService.sendPasswordResetEmail(
                    user.getEmail(), resetLink);
        }

        showNotification(successMsg, true);
        emailField.clear();
    }

    private void showNotification(
            String msg, boolean success) {
        Notification n = Notification.show(msg, 4000,
                Notification.Position.MIDDLE);
        n.addThemeVariants(success
                ? NotificationVariant.LUMO_SUCCESS
                : NotificationVariant.LUMO_ERROR);
    }
}