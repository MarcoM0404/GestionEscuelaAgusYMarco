package com.example.app.base.ui.view;

import com.example.app.base.domain.Course;
import com.example.app.base.domain.Seat;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.OptionalDouble;

@Route(value = "professor", layout = MainLayout.class)
public class ProfessorView extends VerticalLayout {

    private final StudentService   studentService;
    private final SeatService      seatService;

    private final Grid<Course> courseGrid = new Grid<>(Course.class, false);

    @Autowired
    public ProfessorView(ProfessorService profService,
                         StudentService studentService,
                         CourseService courseService,
                         SeatService seatService) {
        this.studentService = studentService;
        this.seatService    = seatService;


        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.PROFESSOR) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();


        add(
          new H2("👨‍🏫 Bienvenido, " + u.getUsername()),
          new Button("✏️ Mi Perfil", e ->
              UI.getCurrent().navigate("professor/profile")
          )
        );


        courseGrid.addColumn(Course::getId)
                  .setHeader("ID")
                  .setWidth("70px");
        courseGrid.addColumn(Course::getName)
                  .setHeader("Curso")
                  .setAutoWidth(true);


        courseGrid.addColumn(c ->
            seatService.findByCourseId(c.getId()).size()
        ).setHeader("Inscritos").setAutoWidth(true);


        courseGrid.addColumn(c -> {
            OptionalDouble avgOpt = seatService.findByCourseId(c.getId()).stream()
                .mapToDouble(s -> s.getMark() != null ? s.getMark() : 0.0)
                .average();
            return avgOpt.isPresent()
                ? String.format("%.2f", avgOpt.getAsDouble())
                : "—";
        }).setHeader("Prom. Nota").setAutoWidth(true);


        courseGrid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) {
                openEnrollmentDialog(evt.getValue());
            }
        });

        courseGrid.setSizeFull();
        add(new H2("📋 Mis Cursos"), courseGrid);


        Long profId = profService.findByUserId(u.getId())
                                 .map(p -> p.getId())
                                 .orElse(-1L);
        courseGrid.setItems(courseService.findByProfessorId(profId));
    }

    private void openEnrollmentDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setWidth("700px");
        dialog.add(new H2("📝 Inscripciones: " + course.getName()));


        Select<Student> studentSelect = new Select<>();
        studentSelect.setLabel("Alumno");
        studentSelect.setItems(studentService.findAll());
        studentSelect.setItemLabelGenerator(Student::getName);

        DatePicker inscDate = new DatePicker("Fecha Inscripción");
        inscDate.setValue(java.time.LocalDate.now());

        Button enrollBtn = new Button("➕ Inscribir", e -> {
            Student s = studentSelect.getValue();
            if (s == null) {
                Notification.show("Selecciona un alumno", 2000, Notification.Position.BOTTOM_START);
                return;
            }
            Seat seat = new Seat();
            seat.setStudent(s);
            seat.setCourse(course);
            seat.setYear(inscDate.getValue());
            seatService.save(seat);
            refreshSeatGrid(course, dialog);
        });

        HorizontalLayout toolbar = new HorizontalLayout(
            studentSelect, inscDate, enrollBtn
        );
        
        
        //ver tema del grid!!!!!
        
        Grid<Seat> seatGrid = new Grid<>(Seat.class, false);
        seatGrid.addColumn(Seat::getId).setHeader("ID").setWidth("50px");
        seatGrid.addColumn(s -> s.getStudent().getName()).setHeader("Alumno").setAutoWidth(true);
        seatGrid.addColumn(Seat::getYear).setHeader("Inscripción");
        seatGrid.addColumn(Seat::getEvaluationDate).setHeader("Evaluación");
        seatGrid.addColumn(Seat::getMark).setHeader("Nota");


        seatGrid.addComponentColumn(seat -> {
            Button del = new Button("🗑️", ev -> {
                seatService.deleteById(seat.getId());
                refreshSeatGrid(course, dialog);
            });
            return del;
        }).setHeader("Quitar");


        seatGrid.asSingleSelect().addValueChangeListener(evt -> {
            if (evt.getValue() != null) {
                openEditSeatDialog(evt.getValue(), seatGrid);
            }
        });

        seatGrid.setSizeFull();

        dialog.add(toolbar, seatGrid);
        dialog.open();
        refreshSeatGrid(course, dialog);
    }

    @SuppressWarnings("unchecked")
    private void refreshSeatGrid(Course course, Dialog dialog) {
        dialog.getChildren()
              .filter(c -> c instanceof Grid)
              .map(c -> (Grid<Seat>) c)
              .findFirst()
              .ifPresent(g -> g.setItems(
                  seatService.findByCourseId(course.getId())
              ));
    }

    private void openEditSeatDialog(Seat seat, Grid<Seat> seatGrid) {
        Dialog d = new Dialog();
        d.setWidth("400px");
        d.add(new H2("✏️ Editar Inscripción"));

        NumberField markField = new NumberField("Nota");
        DatePicker evalDate   = new DatePicker("Fecha Evaluación");
        markField.setValue(seat.getMark());
        evalDate.setValue(seat.getEvaluationDate());

        Button save = new Button("Guardar", e -> {
            seat.setMark(markField.getValue());
            seat.setEvaluationDate(evalDate.getValue());
            seatService.save(seat);
            seatGrid.getDataProvider().refreshAll();
            d.close();
        });
        Button cancel = new Button("Cancelar", e -> d.close());

        d.add(new VerticalLayout(
            markField, evalDate,
            new HorizontalLayout(save, cancel)
        ));
        d.open();
    }
}