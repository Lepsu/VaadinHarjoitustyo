package com.example.user.ui;

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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@PageTitle("Vaihda salasana")
@AnonymousAllowed
@Route(value = "reset-password")
public class ResetPasswordView extends Main
        implements BeforeEnterObserver {

    private final PasswordResetTokenRepository tokenRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final PasswordField newPassword =
            new PasswordField("Uusi salasana");
    private final PasswordField confirmPassword =
            new PasswordField("Vahvista salasana");
    private final Button resetBtn =
            new Button("Vaihda salasana");
    private final Paragraph statusMsg = new Paragraph();

    private String token;

    public ResetPasswordView(
            PasswordResetTokenRepository tokenRepository,
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(
                com.vaadin.flow.component.orderedlayout
                        .FlexComponent.Alignment.CENTER);
        layout.setMaxWidth("400px");
        layout.getStyle().set("margin", "0 auto");

        newPassword.setWidthFull();
        newPassword.setHelperText("Vähintään 6 merkkiä");
        confirmPassword.setWidthFull();

        resetBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resetBtn.setWidthFull();
        resetBtn.addClickListener(e -> handleReset());

        layout.add(
                new H2("Vaihda salasana"),
                statusMsg,
                newPassword,
                confirmPassword,
                resetBtn
        );

        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("token",
                        java.util.List.of(""))
                .get(0);

        if (token.isBlank()) {
            statusMsg.setText(
                    "Virheellinen tai vanhentunut linkki.");
            resetBtn.setEnabled(false);
            return;
        }

        Optional<PasswordResetToken> tokenOpt =
                tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()
                || tokenOpt.get().isExpired()
                || tokenOpt.get().isUsed()) {
            statusMsg.setText(
                    "Linkki on vanhentunut tai jo käytetty. "
                            + "Pyydä uusi palautuslinkki.");
            resetBtn.setEnabled(false);
        }
    }

    private void handleReset() {
        if (newPassword.isEmpty()
                || confirmPassword.isEmpty()) {
            showNotification(
                    "Täytä kaikki kentät", false);
            return;
        }

        if (newPassword.getValue().length() < 6) {
            showNotification(
                    "Salasanan oltava vähintään 6 merkkiä",
                    false);
            return;
        }

        if (!newPassword.getValue()
                .equals(confirmPassword.getValue())) {
            showNotification(
                    "Salasanat eivät täsmää", false);
            return;
        }

        Optional<PasswordResetToken> tokenOpt =
                tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            showNotification("Virheellinen token", false);
            return;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(
                newPassword.getValue()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        showNotification(
                "Salasana vaihdettu! Voit nyt kirjautua sisään.",
                true);

        resetBtn.setEnabled(false);
        newPassword.setEnabled(false);
        confirmPassword.setEnabled(false);

        // Ohjataan kirjautumissivulle
        getUI().ifPresent(ui ->
                ui.navigate("login"));
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