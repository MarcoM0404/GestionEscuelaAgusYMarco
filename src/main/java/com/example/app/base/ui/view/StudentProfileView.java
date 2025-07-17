package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "student/profile", layout = MainLayout.class)
public class StudentProfileView extends VerticalLayout {

    private final StudentService  studentService;
    private final PersonService   personService;
    private final AddressService  addressService;

    @Autowired
    public StudentProfileView(StudentService studentService,
                              PersonService personService,
                              AddressService addressService) {
        this.studentService  = studentService;
        this.personService   = personService;
        this.addressService  = addressService;


        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.STUDENT) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        add(new H2("👤 Mi Perfil de Alumno"));


        studentService.findByUserId(u.getId()).ifPresentOrElse(student -> {

            if (student.getAddress() == null) {
                student.setAddress(new Address());
            }

            Binder<Student> binder = new Binder<>(Student.class);

            TextField name    = new TextField("Nombre");
            TextField email   = new TextField("Email");
            TextField phone   = new TextField("Teléfono");
            TextField street  = new TextField("📍Calle");
            TextField city    = new TextField("Ciudad");
            TextField state   = new TextField("Provincia");
            TextField country = new TextField("País");

            binder.forField(name)
                  .asRequired("Requerido")
                  .bind(Student::getName, Student::setName);
            binder.forField(email)
                  .asRequired("Requerido")
                  .bind(Student::getEmail, Student::setEmail);
            binder.forField(phone)
                  .bind(Student::getPhone, Student::setPhone);

            binder.forField(street)
                  .bind(s -> s.getAddress().getStreet(),  (s,v)->s.getAddress().setStreet(v));
            binder.forField(city)
                  .bind(s -> s.getAddress().getCity(),    (s,v)->s.getAddress().setCity(v));
            binder.forField(state)
                  .bind(s -> s.getAddress().getState(),   (s,v)->s.getAddress().setState(v));
            binder.forField(country)
                  .bind(s -> s.getAddress().getCountry(), (s,v)->s.getAddress().setCountry(v));

            FormLayout form = new FormLayout(
                name, email, phone,
                street, city, state, country
            );

            binder.readBean(student);

            Button save = new Button("Guardar cambios", evt -> {
                if (binder.writeBeanIfValid(student)) {
                    addressService.save(student.getAddress());
                    personService.save(student);
                    studentService.save(student);
                    Notification.show("Perfil actualizado", 1500, Notification.Position.BOTTOM_START);
                }
            });

            add(form, save);

        }, () -> {

            Notification.show("Aún no tienes perfil de alumno. Contacta al administrador.", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate(""); 
        });
    }
}