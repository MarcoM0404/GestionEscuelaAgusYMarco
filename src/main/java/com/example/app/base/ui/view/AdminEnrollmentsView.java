package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.example.app.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RolesAllowed("ADMIN")
@PageTitle("Inscripciones")
@Route(value = "admin/enrollments", layout = MainLayout.class)
public class AdminEnrollmentsView extends VerticalLayout {

    private final SeatService    seatService;
    private final CourseService  courseService;
    private final StudentService studentService;

    private final Grid<Seat> grid = new Grid<>(Seat.class, false);
    private final Button     toggleHistoryBtn =
            new Button("Ver historial", VaadinIcon.BOOK.create());

    public AdminEnrollmentsView(SeatService seatService,
                                CourseService courseService,
                                StudentService studentService) {

        this.seatService    = seatService;
        this.courseService  = courseService;
        this.studentService = studentService;

        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != AppRoles.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        buildHeader();
        configureGrid();
        grid.setVisible(false);
        add(grid);
    }


    private void buildHeader() {
        Icon clip = VaadinIcon.CLIPBOARD_TEXT.create();
        clip.getStyle().set("margin-right", "4px");
        H2 titleLbl = new H2("Gestión de Inscripciones");
        HorizontalLayout title = new HorizontalLayout(clip, titleLbl);
        title.setAlignItems(Alignment.CENTER);

        Icon plus = VaadinIcon.PLUS_CIRCLE.create();
        Button addBtn = new Button("Nueva inscripción", plus,
                e -> openEditor(new Seat()));
        addBtn.setIconAfterText(false);

        toggleHistoryBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY,
                                          ButtonVariant.LUMO_CONTRAST);
        toggleHistoryBtn.addClickListener(e -> toggleHistory());

        HorizontalLayout header =
                new HorizontalLayout(title, addBtn, toggleHistoryBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);
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
        if (grid.isVisible()) grid.setItems(seatService.findAllOrdered());
    }

    private void toggleHistory() {
        boolean show = !grid.isVisible();
        grid.setVisible(show);
        if (show) refreshGrid();
        toggleHistoryBtn.setText(show ? "Ocultar historial" : "Ver historial");
    }


    private void openEditor(Seat original) {

        final Seat seat = original;
        if (seat.getStudent() == null) seat.setStudent(new Student());

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(seat.getId() == null ? "Nueva inscripción"
                                                   : "Editar inscripción");
        dialog.setWidth("450px");

        Binder<Seat> binder = new Binder<>(Seat.class);

        ComboBox<Course> courseSelect = new ComboBox<>("Curso");
        courseSelect.setPrefixComponent(VaadinIcon.BOOK.create());
        courseSelect.setItems(courseService.findAll());
        courseSelect.setItemLabelGenerator(Course::getName);
        binder.forField(courseSelect).asRequired("Requerido")
              .bind(Seat::getCourse, Seat::setCourse);

        TextField chosenStudentField = new TextField("Alumno");
        chosenStudentField.setReadOnly(true);
        chosenStudentField.setWidthFull();
        chosenStudentField.setPrefixComponent(VaadinIcon.USER.create());

        Button selectStudentBtn = new Button("Seleccionar alumno");
        selectStudentBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE,
                                          ButtonVariant.LUMO_SMALL);

        final Student[] chosenStudent = { null };
        if (seat.getStudent() != null && seat.getStudent().getId() != null) {
            chosenStudent[0] = seat.getStudent();
            chosenStudentField.setValue(chosenStudent[0].getName());
        }

        selectStudentBtn.addClickListener(e ->
                openStudentPicker(stu -> {
                    chosenStudent[0] = stu;
                    chosenStudentField.setValue(stu.getName());
                })
        );

        VerticalLayout studentBlock = new VerticalLayout(chosenStudentField, selectStudentBtn);
        studentBlock.setPadding(false); studentBlock.setSpacing(false);

        DatePicker yearPicker = new DatePicker("Año");
        NumberField markField = new NumberField("Nota");
        markField.setPrefixComponent(VaadinIcon.STAR.create());

        binder.forField(yearPicker).asRequired("Requerido")
              .bind(Seat::getYear, Seat::setYear);
        binder.forField(markField).bind(Seat::getMark, Seat::setMark);
        binder.readBean(seat);

        Button save = new Button("Guardar", ev -> {
            if (chosenStudent[0] == null) {
                Notification.show("Debes seleccionar un alumno.",
                                  3000, Notification.Position.MIDDLE);
                return;
            }
            seat.setStudent(chosenStudent[0]);

            try {
                if (binder.writeBeanIfValid(seat)) {
                    seatService.save(seat);
                    refreshGrid();
                    dialog.close();
                    Notification.show("Inscripción guardada");
                }
            } catch (SeatService.DuplicateEnrollmentException dup) {
                Notification.show(dup.getMessage(),
                                  3000, Notification.Position.MIDDLE);
            }
        });
        Button cancel = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(new VerticalLayout(courseSelect, studentBlock,
                                      yearPicker, markField, actions));
        dialog.open();
    }


    private void openStudentPicker(Consumer<Student> onSelect) {

        Dialog picker = new Dialog();
        picker.setHeaderTitle("Seleccionar alumno");
        picker.setWidth("600px");
        picker.setHeight("70vh");

        TextField search = new TextField();
        search.setPlaceholder("Buscar por nombre…");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setWidthFull();

        Grid<Student> stuGrid = new Grid<>(Student.class, false);
        stuGrid.addColumn(Student::getName).setHeader("Nombre").setAutoWidth(true);
        stuGrid.addColumn(Student::getEmail).setHeader("Email").setAutoWidth(true);
        stuGrid.addColumn(s -> s.getStudentNumber().toString())
               .setHeader("Matrícula").setAutoWidth(true);
        stuGrid.setSizeFull();

        List<Student> all = studentService.findAll();
        stuGrid.setItems(all);

        search.addValueChangeListener(ev -> {
            String term = ev.getValue().trim().toLowerCase();
            if (term.isBlank()) {
                stuGrid.setItems(all);
            } else {
                stuGrid.setItems(all.stream()
                          .filter(s -> s.getName().toLowerCase().contains(term))
                          .collect(Collectors.toList()));
            }
        });

        stuGrid.addItemDoubleClickListener(ev -> {
            onSelect.accept(ev.getItem());
            picker.close();
        });

        VerticalLayout content = new VerticalLayout(search, stuGrid);
        content.setSizeFull(); content.setPadding(false); content.setSpacing(false);
        content.expand(stuGrid);

        picker.add(content);
        picker.open();
    }
}