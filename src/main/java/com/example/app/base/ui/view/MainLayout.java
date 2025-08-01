package com.example.app.base.ui.view;

import com.example.app.base.domain.User;
import com.example.app.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

public class MainLayout extends AppLayout {

    public MainLayout() {
        addToNavbar(createHeader());
    }

    private HorizontalLayout createHeader() {
        H1 logo = new H1("Gestión Cursos");
        logo.getStyle().set("margin", "0 1rem");

        HorizontalLayout header = new HorizontalLayout(
                logo,
                buildNavTabs()
        );
        header.setAlignItems(Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        return header;
    }

    private HorizontalLayout buildNavTabs() {
        User user = VaadinSession.getCurrent().getAttribute(User.class);
        Tabs tabs = new Tabs();
        tabs.setId("main-tabs");
        tabs.setFlexGrowForEnclosedTabs(1);
        tabs.getStyle()
                .set("gap", "4px")
                .set("font-size", "0.80rem")
                .set("text-transform", "uppercase")
                .set("font-weight", "600");

        if (user != null) {
            switch (user.getRole()) {
                case ADMIN -> {
                    tabs.add(tab("Administradores", AdminAdminsView.class));
                    tabs.add(tab("Cursos",           AdminCoursesView.class));
                    tabs.add(tab("Profesores",       AdminProfessorsView.class));
                    tabs.add(tab("Alumnos",          AdminStudentsView.class));
                    tabs.add(tab("Inscripciones",    AdminEnrollmentsView.class));
                }
                case PROFESSOR -> {
                    tabs.add(tab("Panel Profesor",  ProfessorView.class));
                    tabs.add(tab("Mi Perfil",       ProfessorProfileView.class));
                }
                case STUDENT -> {
                    tabs.add(tab("Panel Alumno",    StudentView.class));
                    tabs.add(tab("Inscripciones",   StudentEnrollmentsView.class));
                    tabs.add(tab("Mi Perfil",       StudentProfileView.class));
                }
            }

            Button logoutButton = new Button("Cerrar sesión", e -> {
                UI.getCurrent().getPage().setLocation("/perform-logout");
            });
            Tab logoutTab = new Tab(logoutButton);
            styleSingleTab(logoutTab);
            tabs.add(logoutTab);

        }

        HorizontalLayout box = new HorizontalLayout(tabs);
        box.getStyle()
                .set("background", "#ffffff")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,.15)")
                .set("padding", "4px 10px");
        box.setPadding(false);
        box.setMargin(false);
        return box;
    }

    private Tab tab(String label, Class<? extends com.vaadin.flow.component.Component> target) {
        RouterLink link = new RouterLink(label, target);
        link.setHighlightCondition((l, e) ->
                e.getLocation().getFirstSegment().equals(link.getHref()));
        Tab t = new Tab(link);
        styleSingleTab(t);
        return t;
    }

    private void styleSingleTab(Tab t) {
        t.getStyle()
                .set("border-radius", "4px")
                .set("padding", "2px 8px")
                .set("transition", "box-shadow 120ms ease");
        t.getElement().setAttribute("onmouseover",
                "this.style.boxShadow='0 2px 6px rgba(0,0,0,.18)';");
        t.getElement().setAttribute("onmouseout",
                "this.style.boxShadow='';");
    }
}