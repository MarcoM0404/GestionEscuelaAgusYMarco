package com.example.app.base.ui.view;

import com.example.app.base.domain.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
    }

    private void createHeader() {
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        H1 logo = new H1("Gestión Cursos");
        logo.getStyle().set("margin", "0 1rem");

        HorizontalLayout header = new HorizontalLayout(
            logo,
            createNavLinks(user)
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();

        addToNavbar(header);
    }

    private HorizontalLayout createNavLinks(User user) {
        HorizontalLayout nav = new HorizontalLayout();

        if (user != null) {
            switch (user.getRole()) {
                case ADMIN:
                    nav.add(
                        new RouterLink("Cursos",        AdminCoursesView.class),
                        new RouterLink("Profesores",    AdminProfessorsView.class),
                        new RouterLink("Alumnos",       AdminStudentsView.class),
                        new RouterLink("Inscripciones", AdminEnrollmentsView.class)
                    );
                    break;
                case PROFESSOR:
                    nav.add(
                        new RouterLink("Panel Profesor", ProfessorView.class),
                        new RouterLink("Mi Perfil",      ProfessorProfileView.class)
                    );
                    break;
                case STUDENT:
                    nav.add(
                        new RouterLink("Panel Alumno",        StudentView.class),
                        new RouterLink("Mis Inscripciones",   StudentEnrollmentsView.class),
                        new RouterLink("Mi Perfil",           StudentProfileView.class)
                    );
                    break;
            }
            Button logout = new Button("Cerrar sesión", e -> {
                VaadinSession.getCurrent().close();
                UI.getCurrent().navigate("login");
            });
            logout.getStyle().set("margin-left", "1rem");
            nav.add(logout);
        }

        return nav;
    }
}
