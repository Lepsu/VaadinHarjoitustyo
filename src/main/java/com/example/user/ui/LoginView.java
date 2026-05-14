package com.example.user.ui;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.router.RouterLink;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticationContext authenticationContext;

    public LoginView(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        setAction(RouteUtil.getRoutePath(
                VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("EventApp");
        i18n.getHeader().setDescription(
                "Kirjaudu sisään käyttääksesi sovellusta");

        // Lisää rekisteröitymislinkki additional information -kenttään
        i18n.setAdditionalInformation(
                "Ei vielä tiliä? Rekisteröidy osoitteessa /register");

        setI18n(i18n);
        setForgotPasswordButtonVisible(false);
        setOpened(true);

        // Lisää rekisteröidy-nappi
        getElement().executeJs(
                "this.querySelector('[slot=\"footer\"]')");

        RouterLink registerLink = new RouterLink(
                "Rekisteröidy tästä",
                com.example.user.ui.RegisterView.class);
        registerLink.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)");

        RouterLink forgotLink = new RouterLink(
                "Unohtunut salasana?",
                ForgotPasswordView.class);
        forgotLink.getStyle()
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-s)");

        getFooter().add(registerLink, forgotLink);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticationContext.isAuthenticated()) {
            setOpened(false);
            event.forwardTo("");
        }
        setError(event.getLocation().getQueryParameters()
                .getParameters().containsKey("error"));
    }
}
