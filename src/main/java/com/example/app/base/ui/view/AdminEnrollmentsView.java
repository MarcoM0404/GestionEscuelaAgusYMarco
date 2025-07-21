package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
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

    private final Grid<Seat> grid = new Grid<>(Seat.class, false);

    private final Button toggleHistoryBtn = new Button("📑 Ver historial");

    public AdminEnrollmentsView(SeatService seatService,
                                CourseService courseService,
                                StudentService studentService) {

        this.seatService    = seatService;
        this.courseService  = courseService;
        this.studentService = studentService;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        H2 title = new H2("📝 Gestión de Inscripciones");

        Button addBtn = new Button("➕ Nueva inscripción",
                                   e -> openEditor(new Seat()));

        toggleHistoryBtn.addClickListener(e -> toggleHistory());

        HorizontalLayout header = new HorizontalLayout(title, addBtn, toggleHistoryBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);

        configureGrid();
        grid.setVisible(false);
        add(grid);
    }

    private void configureGrid() {
        grid.addColumn(Seat::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(s -> s.getCourse().getName()).setHeader("Curso");
        grid.addColumn(s -> s.getStudent().getName()).setHeader("Alumno");
        grid.addColumn(Seat::getYear).setHeader("Año");
        grid.addColumn(Seat::getMark).setHeader("Nota");

        grid.setSizeFull();

        grid.addItemDoubleClickListener(ev -> openEditor(ev.getItem()));
    }

    private void refreshGrid() {
        if (grid.isVisible()) {
            grid.setItems(seatService.findAll());
        }
    }

    private void toggleHistory() {
        boolean show = !grid.isVisible();
        grid.setVisible(show);
        if (show) {
            refreshGrid();
            toggleHistoryBtn.setText("📂 Ocultar historial");
        } else {
            toggleHistoryBtn.setText("📑 Ver historial");
        }
    }

    private void openEditor(Seat original) {
        final Seat seat = original;

        if (seat.getCourse() == null)  seat.setCourse(new Course());
        if (seat.getStudent() == null) seat.setStudent(new Student());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(seat.getId() == null ? "Nueva inscripción" : "Editar inscripción");
        dialog.setWidth("450px");

        Binder<Seat> binder = new Binder<>(Seat.class);

        Select<Course>  courseSelect  = new Select<>();
        Select<Student> studentSelect = new Select<>();
        DatePicker      yearPicker    = new DatePicker("Año");
        NumberField     markField     = new NumberField("Nota");

        courseSelect.setLabel("Curso");
        courseSelect.setItems(courseService.findAll());
        courseSelect.setItemLabelGenerator(Course::getName);

        studentSelect.setLabel("Alumno");
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
                Notification.show("Inscripción guardada");
            }
        });
        Button cancel = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
            courseSelect, studentSelect,
            yearPicker, markField,
            new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }
}
