package com.example.app.base.ui.view;

import com.vaadin.flow.component.UI;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Panel Admin")
@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {
    public AdminView() {
        UI.getCurrent().navigate("admin/courses");
    }
}