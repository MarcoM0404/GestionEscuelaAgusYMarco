package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "admin/admins", layout = MainLayout.class)
public class AdminAdminsView extends VerticalLayout {

    private final UserService          userService;
    private final AdministratorService adminService;
    private final PasswordEncoder      encoder;

    private final Grid<Administrator> grid   = new Grid<>(Administrator.class, false);
    private final TextField           filter = new TextField();

    public AdminAdminsView(UserService userService,
                           AdministratorService adminService,
                           PasswordEncoder encoder) {

        this.userService  = userService;
        this.adminService = adminService;
        this.encoder      = encoder;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        H2 title = new H2("👑 Gestión de Administradores");

        Button addBtn = new Button("➕ Nuevo Admin",
                                   e -> openEditor(new Administrator(), true));

        filter.setPlaceholder("Buscar admin…");
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);

        add(header);

        configureGrid();
        add(grid);
        applyFilter("");
    }

    private void configureGrid() {
        grid.addColumn(a -> {
            User u = a.getUser();
            return u != null ? u.getId() : "";
        }).setHeader("ID").setWidth("70px");

        grid.addColumn(a -> {
            User u = a.getUser();
            return u != null ? u.getUsername() : "(sin usuario)";
        }).setHeader("Usuario").setAutoWidth(true);

        grid.addColumn(Person::getName).setHeader("Nombre");
        grid.addColumn(Person::getEmail).setHeader("Email");
        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(ev -> {
            if (ev.getValue() != null) openEditor(ev.getValue(), false);
        });
    }

    private void applyFilter(String term){
        grid.setItems(adminService.search(term));
    }

    private void openEditor(Administrator admin, boolean isNew) {
        Dialog dialog = new Dialog();
        dialog.setWidth("420px");

        Binder<Administrator> binder = new Binder<>(Administrator.class);

        TextField     name  = new TextField("Nombre");
        TextField     email = new TextField("Email");
        TextField     usern = new TextField("Usuario");
        PasswordField pass  = new PasswordField("Contraseña");

        binder.forField(name).asRequired().bind(Administrator::getName, Administrator::setName);
        binder.forField(email).asRequired().bind(Administrator::getEmail, Administrator::setEmail);

        if (!isNew) {
            usern.setValue(admin.getUser().getUsername());
            usern.setReadOnly(true);
            pass.setPlaceholder("Dejar vacío para no cambiar");
        }

        binder.readBean(admin);

        Button save = new Button("Guardar", e -> {
            if (binder.writeBeanIfValid(admin)) {

                User user;
                if (isNew) {
                    if (userService.existsByUsername(usern.getValue())) {
                        usern.setInvalid(true);
                        usern.setErrorMessage("Usuario ya existe");
                        return;
                    }
                    user = userService.createAdmin(
                            usern.getValue(),
                            pass.getValue().isBlank() ? usern.getValue() : pass.getValue(),
                            encoder
                    );
                    admin.setUser(user);
                } else {
                    user = admin.getUser();
                    if (!pass.getValue().isBlank()) {
                        user.setPassword(encoder.encode(pass.getValue()));
                        userService.save(user);
                    }
                }

                adminService.save(admin);
                applyFilter(filter.getValue());
                dialog.close();
            }
        });
        Button cancel = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                usern, pass, name, email,
                new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }
}
