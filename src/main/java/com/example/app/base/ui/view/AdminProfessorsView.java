package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.example.app.security.AppRoles;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@RolesAllowed("ADMIN")
@PageTitle("Profesores")
@Route(value = "admin/professors", layout = MainLayout.class)
public class AdminProfessorsView extends VerticalLayout {

    private final ProfessorService profService;
    private final UserService      userService;
    private final PasswordEncoder  passwordEncoder;

    private final Grid<Professor> grid   = new Grid<>(Professor.class, false);
    private final TextField       filter = new TextField();

    public AdminProfessorsView(ProfessorService profService,
                               UserService userService,
                               PasswordEncoder passwordEncoder) {

        this.profService     = profService;
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != AppRoles.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        buildHeader();
        configureGrid();
        add(grid);
        applyFilter("");
    }

    private void buildHeader() {
        Icon cap = VaadinIcon.ACADEMY_CAP.create();
        cap.getStyle().set("margin-right", "4px");
        H2 titleLbl = new H2("Gestión de Profesores");
        HorizontalLayout title = new HorizontalLayout(cap, titleLbl);
        title.setAlignItems(Alignment.CENTER);

        filter.setPlaceholder("Buscar profesor…");
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        Icon plus = VaadinIcon.PLUS_CIRCLE.create();
        Button addBtn = new Button("Nuevo Profesor", plus,
                e -> openEditor(new Professor()));
        addBtn.setIconAfterText(false);

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);
    }

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


    private void openEditor(Professor selected) {

        Professor loaded = selected.getId() != null
                ? profService.findWithUserById(selected.getId()).orElse(selected)
                : selected;

        final Professor prof = loaded;

        if (prof.getAddress() == null) prof.setAddress(new Address());
        if (prof.getUser()    == null) prof.setUser(new User());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((prof.getId() == null ? "Nuevo" : "Editar") + " profesor");
        dialog.setWidth("50%"); dialog.setMaxWidth("800px");

        Binder<Professor> binder = new Binder<>(Professor.class);

        TextField email = new TextField("Email");
        email.setPrefixComponent(VaadinIcon.ENVELOPE.create());

        TextField username = new TextField("Usuario");
        username.setPrefixComponent(VaadinIcon.USER.create());

        PasswordField password = new PasswordField("Contraseña");
        password.setPrefixComponent(VaadinIcon.LOCK.create());

        TextField name = new TextField("Nombre");
        name.setPrefixComponent(VaadinIcon.USER_CHECK.create());

        TextField phone = new TextField("Teléfono");
        phone.setPrefixComponent(VaadinIcon.PHONE.create());

        NumberField salary = new NumberField("Salario");
        salary.setPrefixComponent(VaadinIcon.MONEY.create());

        TextField street  = new TextField("Calle");
        street.setPrefixComponent(VaadinIcon.ROAD.create());

        TextField city    = new TextField("Ciudad");
        city.setPrefixComponent(VaadinIcon.BUILDING.create());

        TextField state   = new TextField("Provincia");
        state.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

        TextField country = new TextField("País");
        country.setPrefixComponent(VaadinIcon.GLOBE_WIRE.create());

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

        Button save = new Button("Guardar", ev -> {
            if (!binder.writeBeanIfValid(prof)) return;

            User usr = prof.getUser();
            usr.setRole(AppRoles.PROFESSOR);

            if (!password.getValue().isBlank()) {
                usr.setPassword(passwordEncoder.encode(password.getValue()));
            }
            userService.save(usr);
            profService.save(prof);

            applyFilter(filter.getValue());
            dialog.close();
            Notification.show("Profesor guardado");
        });
        Button cancel = new Button("Cerrar", e -> dialog.close());

        VerticalLayout col1 = new VerticalLayout(email, username, password);
        VerticalLayout col2 = new VerticalLayout(name, phone, salary);
        VerticalLayout col3 = new VerticalLayout(street, city, state, country);
        for (VerticalLayout col : new VerticalLayout[]{col1,col2,col3}) {
            col.setPadding(false); col.setSpacing(false);
        }

        HorizontalLayout columns = new HorizontalLayout(col1, col2, col3);
        columns.setWidthFull(); columns.setSpacing(true);
        columns.setPadding(false);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(columns, actions);
        dialog.open();
    }


    private void confirmDeleteProfessor(Professor prof) {
        ConfirmDialog cd = new ConfirmDialog();
        cd.setHeader("Eliminar profesor");
        cd.setText("¿Seguro que deseas eliminar a " + prof.getName() + "?");
        cd.setCancelText("Cancelar");
        cd.setConfirmText("Eliminar");
        cd.addConfirmListener(e -> {
            profService.deleteById(prof.getId());
            applyFilter(filter.getValue());
        });
        cd.open();
    }
}