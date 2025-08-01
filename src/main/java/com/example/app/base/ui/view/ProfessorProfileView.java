package com.example.app.base.ui.view;
import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.example.app.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import jakarta.annotation.security.RolesAllowed;
@RolesAllowed("PROFESSOR")
@PageTitle("Mi Perfil - Profesor")
@Route(value = "professor/profile", layout = MainLayout.class)
public class ProfessorProfileView extends VerticalLayout {

    private final ProfessorService profService;
    private final PersonService    personService;
    private final AddressService   addressService;

    public ProfessorProfileView(ProfessorService profService,
                                PersonService personService,
                                AddressService addressService) {

        this.profService    = profService;
        this.personService  = personService;
        this.addressService = addressService;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != AppRoles.PROFESSOR) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        H2 header = new H2();
        header.add(new Icon(VaadinIcon.USER), new Span(" Mi Perfil"));
        add(header);

        profService.findByUserId(u.getId()).ifPresentOrElse(me -> {
            if (me.getAddress() == null) me.setAddress(new Address());
            Binder<Professor> binder = new Binder<>(Professor.class);

            TextField   name    = new TextField("Nombre");
            TextField   email   = new TextField("Email");
            TextField   phone   = new TextField("Teléfono");
            NumberField salary  = new NumberField("Salario");
            TextField   street  = new TextField("Calle");
            TextField   city    = new TextField("Ciudad");
            TextField   state   = new TextField("Provincia");
            TextField   country = new TextField("País");
            name.setPrefixComponent(new Icon(VaadinIcon.USER));
            email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
            phone.setPrefixComponent(new Icon(VaadinIcon.PHONE));
            salary.setPrefixComponent(new Icon(VaadinIcon.MONEY));
            street.setPrefixComponent(new Icon(VaadinIcon.ROAD));
            city.setPrefixComponent(new Icon(VaadinIcon.BUILDING));
            state.setPrefixComponent(new Icon(VaadinIcon.FLAG));
            country.setPrefixComponent(new Icon(VaadinIcon.GLOBE));

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
            binder.readBean(me);

            FormLayout form = new FormLayout(name, email, phone,
                                             salary, street, city, state, country);

            Button save = new Button("Guardar", e -> {
                if (binder.writeBeanIfValid(me)) {
                    addressService.save(me.getAddress());
                    personService.save(me);
                    profService.save(me);
                    Notification.show("Perfil guardado",
                                      1500, Notification.Position.BOTTOM_START);
                }
            });
            save.setIcon(new Icon(VaadinIcon.CHECK));
            add(form, save);
        }, () -> {
            Notification.show("Perfil no encontrado", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("");
        });
    }
}