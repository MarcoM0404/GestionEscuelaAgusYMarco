package com.example.app.base.ui.view;

import com.example.app.base.domain.Seat;
import com.example.app.base.domain.User;
import com.example.app.base.service.SeatService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;

@Route(value = "student/enrollments", layout = MainLayout.class)
public class StudentEnrollmentsView extends VerticalLayout {

    private final SeatService seatService;
    private final Grid<Seat>  grid = new Grid<>(Seat.class, false);

    public StudentEnrollmentsView(SeatService seatService) {
        this.seatService = seatService;
        setSizeFull();


        H2 header = new H2();
        header.add(new Icon(VaadinIcon.CLIPBOARD_TEXT), new Span(" Mis Inscripciones"));
        add(header);


        grid.addColumn(Seat::getId)
            .setHeader("ID")
            .setWidth("70px");
        grid.addColumn(s -> s.getCourse().getName())
            .setHeader("Curso")
            .setAutoWidth(true);
        grid.addColumn(Seat::getYear)
            .setHeader("Año");
        grid.addColumn(Seat::getMark)
            .setHeader("Nota");
        grid.setSizeFull();

        add(grid);

        User current = VaadinSession.getCurrent().getAttribute(User.class);
        if (current == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
        } else {
            List<Seat> inscripciones =
                seatService.findByStudentUserId(current.getId());
            grid.setItems(inscripciones);
        }
    }
}
