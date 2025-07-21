package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "admin/professors", layout = MainLayout.class)
public class AdminProfessorsView extends VerticalLayout {

    private final ProfessorService profService;
    private final UserService      userService;
    private final PasswordEncoder  passwordEncoder;

    private final Grid<Professor> grid   = new Grid<>(Professor.class, false);
    private final TextField       filter = new TextField();

    @Autowired
    public AdminProfessorsView(ProfessorService profService,
                               UserService userService,
                               PasswordEncoder passwordEncoder) {

        this.profService     = profService;
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        H2 title  = new H2("👩‍🏫 Gestión de Profesores");

        Button addBtn = new Button("➕ Nuevo Profesor",
                                   e -> openEditor(new Professor()));

        filter.setPlaceholder("Buscar profesor…");
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);

        /* grid */
        configureGrid();
        add(grid);
        applyFilter("");
    }

    /* -------------------------- LISTADO -------------------------- */

    private void configureGrid() {
        grid.addColumn(Professor::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Professor::getName).setHeader("Nombre");
        grid.addColumn(Professor::getEmail).setHeader("Email");
        grid.addColumn(Professor::getPhone).setHeader("Teléfono");
        grid.addColumn(Professor::getSalary).setHeader("Salario");

        grid.addColumn(new ComponentRenderer<>(prof -> {
            Icon trash = VaadinIcon.TRASH.create();
            trash.getStyle().set("cursor", "pointer")
                            .set("color", "var(--lumo-error-color)");
            trash.addClickListener(e -> confirmDeleteProfessor(prof));
            return trash;
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.setSizeFull();
        grid.addItemDoubleClickListener(ev -> openEditor(ev.getItem()));
    }

    private void applyFilter(String term) {
        grid.setItems(term == null || term.isBlank()
            ? profService.findAll()
            : profService.search(term));
    }

    /* -------------------------- FORMULARIO -------------------------- */

    private void openEditor(Professor selected) {

        Professor loaded = selected.getId() != null
                ? profService.findWithUserById(selected.getId()).orElse(selected)
                : selected;

        final Professor prof = loaded; // effectively-final

        if (prof.getAddress() == null) prof.setAddress(new Address());
        if (prof.getUser()    == null) prof.setUser(new User());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((prof.getId() == null ? "Nuevo" : "Editar")
                              + " profesor");
        dialog.setWidth("50%");      // ⇽⇽ 50 % de la pantalla
        dialog.setMaxWidth("800px"); //    (límite opcional)

        Binder<Professor> binder = new Binder<>(Professor.class);

        // ----------- campos -----------
        TextField   name     = new TextField("Nombre");
        TextField   email    = new TextField("Email");
        TextField   phone    = new TextField("Teléfono");
        NumberField salary   = new NumberField("Salario");
        TextField   username = new TextField("Usuario");
        PasswordField password = new PasswordField("Contraseña");

        TextField street  = new TextField("Calle");
        TextField city    = new TextField("Ciudad");
        TextField state   = new TextField("Provincia");
        TextField country = new TextField("País");

        // ----------- binder -----------
        binder.forField(name).asRequired("Requerido")
              .bind(Professor::getName, Professor::setName);
        binder.forField(email).asRequired("Requerido")
              .bind(Professor::getEmail, Professor::setEmail);
        binder.forField(phone).bind(Professor::getPhone, Professor::setPhone);
        binder.forField(salary).asRequired("Requerido")
              .bind(Professor::getSalary, Professor::setSalary);

        binder.forField(street).bind(p -> p.getAddress().getStreet(),
                                     (p,v) -> p.getAddress().setStreet(v));
        binder.forField(city).bind(p -> p.getAddress().getCity(),
                                   (p,v) -> p.getAddress().setCity(v));
        binder.forField(state).bind(p -> p.getAddress().getState(),
                                    (p,v) -> p.getAddress().setState(v));
        binder.forField(country).bind(p -> p.getAddress().getCountry(),
                                      (p,v) -> p.getAddress().setCountry(v));

        binder.forField(username).asRequired("Requerido")
              .bind(p -> p.getUser().getUsername(),
                    (p,v) -> p.getUser().setUsername(v));

        binder.readBean(prof);

        // ----------- acciones -----------
        Button save = new Button("Guardar", ev -> {
            if (binder.writeBeanIfValid(prof)) {

                User usr = prof.getUser();
                usr.setRole(Role.PROFESSOR);

                if (!password.getValue().isBlank()) {
                    usr.setPassword(passwordEncoder.encode(password.getValue()));
                }
                userService.save(usr);
                profService.save(prof);

                applyFilter(filter.getValue());
                dialog.close();
                Notification.show("Profesor guardado");
            }
        });
        Button cancel = new Button("Cerrar", e -> dialog.close());

        /* ------------------- LAYOUT EN 3 COLUMNAS ------------------- */

        // Columna 1 (email, usuario, contraseña)
        VerticalLayout col1 = new VerticalLayout(email, username, password);
        col1.setPadding(false);
        col1.setSpacing(false);

        // Columna 2 (nombre, teléfono, salario)
        VerticalLayout col2 = new VerticalLayout(name, phone, salary);
        col2.setPadding(false);
        col2.setSpacing(false);

        // Columna 3 (dirección)
        VerticalLayout col3 = new VerticalLayout(street, city, state, country);
        col3.setPadding(false);
        col3.setSpacing(false);

        HorizontalLayout columns = new HorizontalLayout(col1, col2, col3);
        columns.setWidthFull();
        columns.setSpacing(true);
        columns.setPadding(false);
        columns.setAlignItems(Alignment.START);
        columns.setFlexGrow(1, col1, col2, col3);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(columns, actions);
        dialog.open();
    }

    /* -------------------------- BORRADO -------------------------- */

    private void confirmDeleteProfessor(Professor prof) {

        ConfirmDialog cd = new ConfirmDialog();
        cd.setHeader("Eliminar profesor");
        cd.setText("¿Estás seguro de que deseas eliminar a "
                   + prof.getName() + "?");
        cd.setCancelText("Cancelar");
        cd.setConfirmText("Eliminar");

        cd.addConfirmListener(e -> {
            profService.deleteById(prof.getId());
            applyFilter(filter.getValue());
        });
        cd.open();
    }
}
