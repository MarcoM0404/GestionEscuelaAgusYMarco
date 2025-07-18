package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Route(value = "admin/students", layout = MainLayout.class)
public class AdminStudentsView extends VerticalLayout {

    private final StudentService   studentService;
    private final PersonService    personService;
    private final AddressService   addressService;
    private final UserService      userService;
    private final PasswordEncoder  passwordEncoder;

    private final Grid<Student> grid   = new Grid<>(Student.class, false);
    private final TextField     filter = new TextField();

    @Autowired
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
        if (u == null || u.getRole() != Role.ADMIN) {
        	getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        setSizeFull();

        H2 title = new H2("👩‍🎓 Gestión de Alumnos");

        Button addBtn = new Button("➕ Nuevo Alumno",
                                   e -> openEditor(new Student()));

        filter.setPlaceholder("Buscar alumno…");
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
        grid.addColumn(Student::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Student::getName).setHeader("Nombre");
        grid.addColumn(Student::getEmail).setHeader("Email");
        grid.addColumn(Student::getStudentNumber).setHeader("Matrícula");
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) openEditor(evt.getValue());
        });
    }

    private void applyFilter(String term) {
        if (term == null || term.isBlank()) {
            grid.setItems(studentService.findAll());
        } else {
            String t = term.toLowerCase();
            grid.setItems(studentService.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(t)
                          || s.getEmail().toLowerCase().contains(t))
                .toList());
        }
    }

    private void openEditor(Student student) {
        if (student.getAddress() == null)  student.setAddress(new Address());
        if (student.getUser()    == null)  student.setUser(new User());
        if (student.getStudentNumber() == null) student.setStudentNumber(UUID.randomUUID());

        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Binder<Student> binder = new Binder<>(Student.class);

        TextField     name      = new TextField("Nombre");
        TextField     email     = new TextField("Email");
        TextField     phone     = new TextField("Teléfono");
        TextField     username  = new TextField("Username");
        PasswordField password  = new PasswordField("Password");
        TextField     street    = new TextField("Calle");
        TextField     city      = new TextField("Ciudad");
        TextField     state     = new TextField("Provincia");
        TextField     country   = new TextField("País");

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

            User u2 = student.getUser();
            User existing = userService.findByUsername(u2.getUsername());
            if (existing != null && (u2.getId() == null || !existing.getId().equals(u2.getId()))) {
                Notification.show("El nombre de usuario ya existe, elige otro.", 3000,
                                  Notification.Position.MIDDLE);
                return;
            }

            u2.setRole(Role.STUDENT);
            if (!password.getValue().isBlank()) {
                u2.setPassword(passwordEncoder.encode(password.getValue()));
            }

            try {
                User savedUser = userService.save(u2);
                student.setUser(savedUser);
                studentService.save(student);
                applyFilter(filter.getValue());
                dialog.close();
                Notification.show("Alumno guardado");
            } catch (DataIntegrityViolationException ex) {
                Notification.show("Error al guardar: "
                                  + ex.getRootCause().getMessage(), 4000,
                                  Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button("Cancelar", ev -> dialog.close());

        dialog.add(new VerticalLayout(
            name, email, phone,
            username, password,
            street, city, state, country,
            new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }
}
