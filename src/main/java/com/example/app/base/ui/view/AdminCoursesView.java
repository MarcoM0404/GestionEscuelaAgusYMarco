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

    private final Grid<Course> grid   = new Grid<>(Course.class, false);
    private final TextField    filter = new TextField();

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

        H2 title = new H2("📚 Gestión de Cursos");

        Button addBtn = new Button("➕ Nuevo Curso",
                                   e -> openEditor(new Course()));

        filter.setPlaceholder("Buscar curso…");
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);

        add(header);

        configureGrid();
        add(grid);
        applyFilter(""); 
    }

    private void configureGrid() {
        grid.addColumn(Course::getId).setHeader("ID").setWidth("70px");
        grid.addColumn(Course::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(c -> c.getProfessor() != null
                ? c.getProfessor().getName()
                : "(sin prof.)")
            .setHeader("Profesor");

        grid.setSizeFull();
        grid.asSingleSelect().addValueChangeListener(ev -> {
            if (ev.getValue() != null) openEditor(ev.getValue());
        });
    }

    private void applyFilter(String term) {
        if (term == null || term.isBlank()) {
            grid.setItems(courseService.findAll());
        } else {
            String t = term.toLowerCase();
            grid.setItems(courseService.findAll().stream()
                .filter(c -> c.getName().toLowerCase().contains(t))
                .toList());
        }
    }

    private void openEditor(Course course) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        Binder<Course> binder = new Binder<>(Course.class);
        TextField      nameField   = new TextField("Nombre");
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
                applyFilter(filter.getValue());
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
