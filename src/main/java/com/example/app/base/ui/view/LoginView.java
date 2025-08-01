package com.example.app.base.ui.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.login.LoginOverlay;
import com.example.app.security.SecurityUtils;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route("login")
@PageTitle("Login")
public class LoginView extends Div implements BeforeEnterObserver {
  private final LoginOverlay login = new LoginOverlay();

  public LoginView() {
    login.setAction("login");
    login.setOpened(true);
    login.setForgotPasswordButtonVisible(false);
    add(login);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (SecurityUtils.isUserLoggedIn()) {

      String role = SecurityUtils.getUserRole();
      switch (role) {
        case "ROLE_ADMIN":
          event.forwardTo(AdminView.class);
          break;
        case "ROLE_PROFESSOR":
          event.forwardTo(ProfessorView.class);
          break;
        case "ROLE_STUDENT":
          event.forwardTo(StudentView.class);
          break;
        default:
          event.forwardTo(MainView.class);
      }
      return;
    }
    if (event.getLocation().getQueryParameters()
              .getParameters().containsKey("error")) {
      login.setError(true);
    }
  }
}