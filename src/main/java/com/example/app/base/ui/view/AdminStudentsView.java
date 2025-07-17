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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Route(value = "admin/students", layout = MainLayout.class)
public class AdminStudentsView extends VerticalLayout {

    private final StudentService   studentService;
    private final PersonService    personService;
    private final AddressService   addressService;
    private final UserService      userService;
    private final PasswordEncoder  passwordEncoder;
    private final Grid<Student>    grid = new Grid<>(Student.class, false);

    @Autowired
    public AdminStudentsView(StudentService studentService,
                             PersonService personService,
                             AddressService addressService,
                             UserService userService,
                             PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.personService  = personService;
        this.addressService = addressService;
        this.userService    = userService;
        this.passwordEncoder = passwordEncoder;


        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        setSizeFull();
        add(new H2("👩‍🎓 Gestión de Alumnos"),
            new Button("➕ Nuevo Alumno", e -> openEditor(new Student())));

        configureGrid();
        add(grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Student::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Student::getName).setHeader("Nombre");
        grid.addColumn(Student::getEmail).setHeader("Email");
        grid.addColumn(Student::getStudentNumber).setHeader("Matrícula");
        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) {
                openEditor(evt.getValue());
            }
        });
    }

    private void refreshGrid() {
        grid.setItems(studentService.findAll());
    }

    private void openEditor(Student student) {

        if (student.getAddress() == null) {
            student.setAddress(new Address());
        }
        if (student.getUser() == null) {
            student.setUser(new User());
        }

        if (student.getStudentNumber() == null) {
            student.setStudentNumber(UUID.randomUUID());
        }


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

        binder.forField(name)
              .asRequired("Requerido")
              .bind(Student::getName, Student::setName);
        binder.forField(email)
              .asRequired("Requerido")
              .bind(Student::getEmail, Student::setEmail);
        binder.forField(phone)
              .bind(Student::getPhone, Student::setPhone);
        binder.forField(street)
              .bind(s -> s.getAddress().getStreet(),
                    (s, v) -> s.getAddress().setStreet(v));
        binder.forField(city)
              .bind(s -> s.getAddress().getCity(),
                    (s, v) -> s.getAddress().setCity(v));
        binder.forField(state)
              .bind(s -> s.getAddress().getState(),
                    (s, v) -> s.getAddress().setState(v));
        binder.forField(country)
              .bind(s -> s.getAddress().getCountry(),
                    (s, v) -> s.getAddress().setCountry(v));


        binder.forField(username)
              .asRequired("Requerido")
              .bind(s -> s.getUser().getUsername(),
                    (s, v) -> s.getUser().setUsername(v));

        binder.readBean(student);

        Button save = new Button("Guardar", ev -> {
            if (binder.writeBeanIfValid(student)) {

                User u2 = student.getUser();
                u2.setRole(Role.STUDENT);
                if (!password.getValue().isBlank()) {
                    u2.setPassword(passwordEncoder.encode(password.getValue()));
                }
                userService.save(u2);


                addressService.save(student.getAddress());
                personService.save(student);
                studentService.save(student);

                refreshGrid();
                dialog.close();
                Notification.show("Alumno guardado");
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