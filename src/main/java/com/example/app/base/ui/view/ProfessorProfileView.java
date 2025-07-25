package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "professor/profile", layout = MainLayout.class)
public class ProfessorProfileView extends VerticalLayout {

    private final ProfessorService profService;
    private final PersonService    personService;
    private final AddressService   addressService;
    private Binder<Professor>      binder;

    @Autowired
    public ProfessorProfileView(ProfessorService profService,
                                PersonService personService,
                                AddressService addressService) {

        this.profService    = profService;
        this.personService  = personService;
        this.addressService = addressService;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.PROFESSOR) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        H2 header = new H2();
        header.add(new Icon(VaadinIcon.USER), new Span(" Mi Perfil"));
        add(header);

        profService.findByUserId(u.getId()).ifPresentOrElse(me -> {
            if (me.getAddress() == null) {
                me.setAddress(new Address());
            }

            binder = new Binder<>(Professor.class);

            TextField   name    = new TextField("Nombre");
            name.setPrefixComponent(new Icon(VaadinIcon.USER));

            TextField   email   = new TextField("Email");
            email.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));

            TextField   phone   = new TextField("Teléfono");
            phone.setPrefixComponent(new Icon(VaadinIcon.PHONE));

            NumberField salary  = new NumberField("Salario");
            salary.setPrefixComponent(new Icon(VaadinIcon.MONEY));

            TextField   street  = new TextField("Calle");
            street.setPrefixComponent(new Icon(VaadinIcon.ROAD));

            TextField   city    = new TextField("Ciudad");
            city.setPrefixComponent(new Icon(VaadinIcon.BUILDING));

            TextField   state   = new TextField("Provincia");
            state.setPrefixComponent(new Icon(VaadinIcon.FLAG));

            TextField   country = new TextField("País");
            country.setPrefixComponent(new Icon(VaadinIcon.GLOBE));

            binder.forField(name)
                  .asRequired("Requerido")
                  .bind(Professor::getName, Professor::setName);
            binder.forField(email)
                  .asRequired("Requerido")
                  .bind(Professor::getEmail, Professor::setEmail);
            binder.forField(phone)
                  .bind(Professor::getPhone, Professor::setPhone);
            binder.forField(salary)
                  .asRequired("Requerido")
                  .bind(Professor::getSalary, Professor::setSalary);

            binder.forField(street)
                  .bind(p -> p.getAddress().getStreet(), (p,v) -> p.getAddress().setStreet(v));
            binder.forField(city)
                  .bind(p -> p.getAddress().getCity(),   (p,v) -> p.getAddress().setCity(v));
            binder.forField(state)
                  .bind(p -> p.getAddress().getState(),  (p,v) -> p.getAddress().setState(v));
            binder.forField(country)
                  .bind(p -> p.getAddress().getCountry(), (p,v) -> p.getAddress().setCountry(v));

            FormLayout form = new FormLayout(
                name, email, phone, salary,
                street, city, state, country
            );

            binder.readBean(me);

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
            Notification.show("Aún no tienes perfil creado. Contacta al administrador.",
                               3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("");
        });
    }
}
