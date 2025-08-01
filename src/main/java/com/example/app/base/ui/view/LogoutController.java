package com.example.app.base.ui.view;

import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/perform-logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // logout de Spring Security
        new SecurityContextLogoutHandler().logout(request, response, null);
        // cerrar sesión de Vaadin si existe
        try {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.close();
            }
        } catch (IllegalStateException ignored) {
        }
        return "redirect:/login?logout";
    }
}