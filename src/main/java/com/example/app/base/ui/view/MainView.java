package com.example.app.base.ui.view;
import com.example.app.base.ui.component.ViewToolbar;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;


@Route
@PermitAll // When security is enabled, allow all authenticated users
public final class MainView extends Main {



    MainView() {
        addClassName(LumoUtility.Padding.MEDIUM);
        add(new ViewToolbar("Main"));
        add(new Div("Please select a view from the menu on the left."));
    }


    public static void showMainView() {
        UI.getCurrent().navigate(MainView.class);
    }
}