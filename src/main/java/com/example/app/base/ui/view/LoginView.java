package com.example.app.base.ui.view;

import com.example.app.base.domain.User;
import com.example.app.base.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "login")
@RouteAlias("")  
public class LoginView extends VerticalLayout {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    public LoginView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService      = userService;
        this.passwordEncoder  = passwordEncoder;

        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);

        setAlignItems(Alignment.CENTER);
        LoginForm loginForm = new LoginForm();
        loginForm.addLoginListener(e -> {
            String username = e.getUsername();
            String rawPass  = e.getPassword();
            User user       = userService.findByUsername(username);


            if (user != null && passwordEncoder.matches(rawPass, user.getPassword())) {
                VaadinSession.getCurrent().setAttribute(User.class, user);
                switch (user.getRole()) {
                  case ADMIN     -> UI.getCurrent().navigate("admin");
                  case PROFESSOR -> UI.getCurrent().navigate("professor");
                  default        -> UI.getCurrent().navigate("student");
                }
            } else {
                loginForm.setError(true);
            }
        });

        add(loginForm);
    }
}
