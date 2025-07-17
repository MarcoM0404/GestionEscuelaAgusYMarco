package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "admin/courses", layout = MainLayout.class)
public class AdminCoursesView extends VerticalLayout {

    private final CourseService    courseService;
    private final ProfessorService profService;
    private final Grid<Course>     grid = new Grid<>(Course.class, false);

    public AdminCoursesView(CourseService courseService,
                            ProfessorService profService) {
        this.courseService = courseService;
        this.profService   = profService;


        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        setSizeFull();
        add(new H2("📚 Gestión de Cursos"),
            new Button("➕ Nuevo Curso", e -> openEditor(new Course())));

        configureGrid();
        add(grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Course::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Course::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> {
            return c.getProfessor() != null
                ? c.getProfessor().getName()
                : "(sin prof.)";
        }).setHeader("Profesor");

        grid.setSizeFull();

        grid.asSingleSelect().addValueChangeListener(ev -> {
            if (ev.getValue() != null) {
                openEditor(ev.getValue());
            }
        });
    }

    private void refreshGrid() {
        grid.setItems(courseService.findAll());
    }

    private void openEditor(Course course) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Binder<Course> binder = new Binder<>(Course.class);
        TextField      nameField  = new TextField("Nombre");
        Select<Professor> profSelect = new Select<>();

        binder.forField(nameField)
              .asRequired("El nombre es obligatorio")
              .bind(Course::getName, Course::setName);

        profSelect.setLabel("Profesor");
        profSelect.setItems(profService.findAll());
        profSelect.setItemLabelGenerator(Professor::getName);
        binder.forField(profSelect)
              .bind(Course::getProfessor, Course::setProfessor);

        binder.readBean(course);

        Button save = new Button("Guardar", ev -> {
            if (binder.writeBeanIfValid(course)) {
                courseService.save(course);
                refreshGrid();
                dialog.close();
            }
        });
        Button cancel = new Button("Cancelar", ev -> dialog.close());

        dialog.add(new VerticalLayout(
            nameField, profSelect,
            new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }
}