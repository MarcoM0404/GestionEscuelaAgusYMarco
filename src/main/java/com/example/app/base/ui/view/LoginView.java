package com.example.app.base.ui.view;

import com.example.app.base.domain.User;
import com.example.app.base.service.UserService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("login")
@RouteAlias("")
public class LoginView extends VerticalLayout {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;

        buildUI();
    }

    private void buildUI() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.STRETCH);
        card.getStyle()
            .set("background", "#eaf4ff")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("padding", "2rem")
            .set("width", "360px")
            .set("max-width", "90%");

        H2 title = new H2("Log in");
        title.getStyle().set("margin", "0").set("text-align", "center");
        card.add(title);
        card.setHorizontalComponentAlignment(Alignment.CENTER, title);

        TextField usernameField = new TextField();
        usernameField.setPlaceholder("Username");
        usernameField.setPrefixComponent(new Icon(VaadinIcon.USER));
        usernameField.setClearButtonVisible(true);
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Password");
        passwordField.setPrefixComponent(new Icon(VaadinIcon.LOCK));
        passwordField.setRevealButtonVisible(false);
        passwordField.setWidthFull();

        Button loginBtn = new Button("Log in",
                e -> authenticate(usernameField.getValue(), passwordField.getValue()));
        loginBtn.addClickShortcut(Key.ENTER);
        loginBtn.setWidthFull();
        loginBtn.getStyle()
                .set("background-color", "#0066ff")
                .set("color", "white");

        Anchor forgot = new Anchor("#", "Forgot password");
        forgot.getStyle().set("font-size", "smaller");
        card.setHorizontalComponentAlignment(Alignment.CENTER, forgot);

        card.add(usernameField, passwordField, loginBtn, forgot);
        add(card);
    }

    private void authenticate(String username, String rawPass) {
        User user = userService.findByUsername(username);

        if (user != null && passwordEncoder.matches(rawPass, user.getPassword())) {
            VaadinSession.getCurrent().setAttribute(User.class, user);
            switch (user.getRole()) {
                case ADMIN     -> UI.getCurrent().navigate("admin");
                case PROFESSOR -> UI.getCurrent().navigate("professor");
                default        -> UI.getCurrent().navigate("student");
            }
        } else {
            Notification.show("Credenciales incorrectas", 3000, Position.MIDDLE);
        }
    }
}