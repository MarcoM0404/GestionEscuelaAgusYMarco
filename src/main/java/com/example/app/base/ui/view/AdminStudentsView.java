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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;

@RolesAllowed("ADMIN")
@PageTitle("Alumnos")
@Route(value = "admin/students", layout = MainLayout.class)
public class AdminStudentsView extends VerticalLayout {

    private final StudentService   studentService;
    private final PersonService    personService;
    private final AddressService   addressService;
    private final UserService      userService;
    private final PasswordEncoder  passwordEncoder;

    private final Grid<Student> grid   = new Grid<>(Student.class, false);
    private final TextField     filter = new TextField();

    public AdminStudentsView(StudentService studentService,
                             PersonService personService,
                             AddressService addressService,
                             UserService userService,
                             PasswordEncoder passwordEncoder) {

        this.studentService  = studentService;
        this.personService   = personService;
        this.addressService  = addressService;
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
        Icon titIcon = VaadinIcon.GROUP.create();
        titIcon.getStyle().set("margin-right", "4px");
        H2 titleLbl = new H2("Gestión de Alumnos");
        HorizontalLayout title = new HorizontalLayout(titIcon, titleLbl);
        title.setAlignItems(Alignment.CENTER);

        filter.setPlaceholder("Buscar alumno…");
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        Icon plus = VaadinIcon.PLUS_CIRCLE.create();
        Button addBtn = new Button("Nuevo Alumno", plus, e -> openEditor(new Student()));
        addBtn.setIconAfterText(false);

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);
    }

    private void configureGrid() {
        grid.addColumn(Student::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Student::getName).setHeader("Nombre");
        grid.addColumn(Student::getEmail).setHeader("Email");
        grid.addColumn(Student::getStudentNumber).setHeader("Matrícula");

        grid.addColumn(new ComponentRenderer<>(student -> {
            Icon trash = VaadinIcon.TRASH.create();
            trash.getStyle().set("cursor", "pointer").set("color", "var(--lumo-error-color)");
            trash.addClickListener(e -> confirmDeleteStudent(student));
            return trash;
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.setSizeFull();
        grid.addItemDoubleClickListener(ev -> openEditor(ev.getItem()));
    }

    private void applyFilter(String term) {
        grid.setItems(studentService.search(term));
    }


    private void openEditor(Student selected) {

        Student loaded = selected.getId() != null
                ? studentService.findWithUserById(selected.getId()).orElse(selected)
                : selected;

        final Student student = loaded;

        if (student.getAddress() == null)  student.setAddress(new Address());
        if (student.getUser()    == null)  student.setUser(new User());
        if (student.getStudentNumber() == null) student.setStudentNumber(UUID.randomUUID());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle((student.getId() == null ? "Nuevo" : "Editar") + " alumno");
        dialog.setWidth("50%"); dialog.setMaxWidth("800px");

        Binder<Student> binder = new Binder<>(Student.class);

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

        TextField street  = new TextField("Calle");
        TextField city    = new TextField("Ciudad");
        TextField state   = new TextField("Provincia");
        TextField country = new TextField("País");
        street.setPrefixComponent(VaadinIcon.ROAD.create());
        city.setPrefixComponent(VaadinIcon.BUILDING.create());
        state.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        country.setPrefixComponent(VaadinIcon.GLOBE_WIRE.create());

        binder.forField(name).asRequired("Requerido")
              .bind(Student::getName, Student::setName);
        binder.forField(email).asRequired("Requerido")
              .bind(Student::getEmail, Student::setEmail);
        binder.forField(phone).bind(Student::getPhone, Student::setPhone);

        binder.forField(street).bind(s -> s.getAddress().getStreet(),
                                     (s,v) -> s.getAddress().setStreet(v));
        binder.forField(city).bind(s -> s.getAddress().getCity(),
                                   (s,v) -> s.getAddress().setCity(v));
        binder.forField(state).bind(s -> s.getAddress().getState(),
                                    (s,v) -> s.getAddress().setState(v));
        binder.forField(country).bind(s -> s.getAddress().getCountry(),
                                      (s,v) -> s.getAddress().setCountry(v));

        binder.forField(username).asRequired("Requerido")
              .bind(s -> s.getUser().getUsername(),
                    (s,v) -> s.getUser().setUsername(v));

        binder.readBean(student);

        Button save = new Button("Guardar", ev -> {
            if (!binder.writeBeanIfValid(student)) return;

            User usr = student.getUser();
            Optional<User> existing = userService.findByUsername(usr.getUsername());
            if (existing.isPresent() && (usr.getId() == null
                    || !existing.get().getId().equals(usr.getId()))) {

                Notification.show("El nombre de usuario ya existe...",
                                  3000, Notification.Position.MIDDLE);
                return;
            }

            usr.setRole(AppRoles.STUDENT);
            if (!password.getValue().isBlank()) {
                usr.setPassword(passwordEncoder.encode(password.getValue()));
            }

            try {
                userService.save(usr);
                student.setUser(usr);
                studentService.save(student);
                applyFilter(filter.getValue());
                dialog.close();
                Notification.show("Alumno guardado");
            } catch (DataIntegrityViolationException ex) {
                Notification.show("Error al guardar: " +
                        ex.getRootCause().getMessage(),
                        4000, Notification.Position.MIDDLE);
            }
        });
        Button cancel = new Button("Cancelar", e -> dialog.close());

        VerticalLayout col1 = new VerticalLayout(email, username, password);
        VerticalLayout col2 = new VerticalLayout(name, phone);
        VerticalLayout col3 = new VerticalLayout(street, city, state, country);
        for (VerticalLayout col : new VerticalLayout[]{col1,col2,col3}) {
            col.setPadding(false); col.setSpacing(false);
        }

        HorizontalLayout columns = new HorizontalLayout(col1, col2, col3);
        columns.setWidthFull(); columns.setSpacing(true); columns.setPadding(false);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(columns, actions);
        dialog.open();
    }


    private void confirmDeleteStudent(Student student) {
        ConfirmDialog cd = new ConfirmDialog();
        cd.setHeader("Eliminar alumno");
        cd.setText("¿Seguro que deseas eliminar a " + student.getName() + "?");
        cd.setCancelText("Cancelar");
        cd.setConfirmText("Eliminar");
        cd.addConfirmListener(e -> {
            studentService.deleteById(student.getId());
            applyFilter(filter.getValue());
        });
        cd.open();
    }
}