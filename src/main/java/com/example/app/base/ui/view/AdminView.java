package com.example.app.base.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "admin", layout = MainLayout.class)
public class AdminView extends VerticalLayout {
    public AdminView() {

        UI.getCurrent().navigate("admin/courses");
    }
}