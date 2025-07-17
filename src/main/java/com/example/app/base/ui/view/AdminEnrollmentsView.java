package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "admin/enrollments", layout = MainLayout.class)
public class AdminEnrollmentsView extends VerticalLayout {
    private final SeatService    seatService;
    private final CourseService  courseService;
    private final StudentService studentService;
    private final Grid<Seat>     grid = new Grid<>(Seat.class, false);

    public AdminEnrollmentsView(SeatService seatService,
                                CourseService courseService,
                                StudentService studentService) {
        this.seatService    = seatService;
        this.courseService  = courseService;
        this.studentService = studentService;

        // Control de acceso
        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        setSizeFull();
        add(new H2("📝 Inscripciones"),
            new Button("➕ Nueva Inscripción", e -> openEditor(new Seat())));

        configureGrid();
        add(grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Seat::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(s -> s.getCourse().getName()).setHeader("Curso");
        grid.addColumn(s -> s.getStudent().getName()).setHeader("Alumno");
        grid.addColumn(Seat::getYear).setHeader("Año");
        grid.addColumn(Seat::getMark).setHeader("Nota");

        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(ev -> {
            if (ev.getValue() != null) {
                openEditor(ev.getValue());
            }
        });
    }

    private void refreshGrid() {
        grid.setItems(seatService.findAll());
    }

    private void openEditor(Seat seat) {
        Dialog dialog = new Dialog();
        dialog.setWidth("450px");

        Binder<Seat> binder = new Binder<>(Seat.class);
        Select<Course>  courseSelect  = new Select<>();
        Select<Student> studentSelect = new Select<>();
        DatePicker      yearPicker     = new DatePicker("Año");
        NumberField     markField      = new NumberField("Nota");

        courseSelect.setItems(courseService.findAll());
        courseSelect.setItemLabelGenerator(Course::getName);
        studentSelect.setItems(studentService.findAll());
        studentSelect.setItemLabelGenerator(Student::getName);

        binder.forField(courseSelect)
              .asRequired("Requerido")
              .bind(Seat::getCourse, Seat::setCourse);
        binder.forField(studentSelect)
              .asRequired("Requerido")
              .bind(Seat::getStudent, Seat::setStudent);
        binder.forField(yearPicker)
              .asRequired("Requerido")
              .bind(Seat::getYear, Seat::setYear);
        binder.forField(markField)
              .bind(Seat::getMark, Seat::setMark);

        binder.readBean(seat);

        Button save = new Button("Guardar", ev -> {
            if (binder.writeBeanIfValid(seat)) {
                seatService.save(seat);
                refreshGrid();
                dialog.close();
            }
        });
        Button cancel = new Button("Cancelar", ev -> dialog.close());

        dialog.add(new VerticalLayout(
            courseSelect, studentSelect,
            yearPicker, markField,
            new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }
}
