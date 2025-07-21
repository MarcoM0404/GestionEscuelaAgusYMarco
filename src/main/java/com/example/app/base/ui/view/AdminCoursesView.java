package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "admin/courses", layout = MainLayout.class)
public class AdminCoursesView extends VerticalLayout {

    private final CourseService   courseService;
    private final ProfessorService profService;
    private final SeatService     seatService;

    private final Grid<Course> grid   = new Grid<>(Course.class, false);
    private final TextField    filter = new TextField();

    public AdminCoursesView(CourseService courseService,
                            ProfessorService profService,
                            SeatService seatService) {

        this.courseService = courseService;
        this.profService   = profService;
        this.seatService   = seatService;

        /* ---- acceso ---- */
        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != Role.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();

        /* ---------- encabezado ---------- */
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

        /* ---------- grid ---------- */
        configureGrid();
        add(grid);
        applyFilter("");
    }

    /* ====================================================== */
    /* Grid                                                   */
    /* ====================================================== */
    private void configureGrid() {

        grid.addColumn(Course::getId)
            .setHeader("ID").setWidth("70px");

        grid.addColumn(Course::getName)
            .setHeader("Nombre").setAutoWidth(true);

        grid.addColumn(c -> c.getProfessor() != null
                ? c.getProfessor().getName()
                : "(sin prof.)")
            .setHeader("Profesor");

        grid.addColumn(c -> seatService.countByCourseId(c.getId()))
            .setHeader("Inscriptos").setWidth("120px");

        /* ---- columna 🗑️ eliminar ---- */
        grid.addColumn(new ComponentRenderer<>(course -> {
            Icon trash = VaadinIcon.TRASH.create();
            trash.getStyle().set("cursor", "pointer")
                            .set("color", "var(--lumo-error-color)");
            trash.addClickListener(e -> confirmDelete(course));
            return trash;
        })).setHeader("")      // sin título
           .setAutoWidth(true)
           .setFlexGrow(0);

        grid.setSizeFull();

        /* doble clic -> diálogo de inscriptos */
        grid.addItemDoubleClickListener(ev -> openSeatsDialog(ev.getItem()));
    }

    /* ====================================================== */
    /* Filtro                                                 */
    /* ====================================================== */
    private void applyFilter(String term) {
        grid.setItems(term == null || term.isBlank()
            ? courseService.findAll()
            : courseService.search(term));
    }

    /* ====================================================== */
    /* Editor de curso                                        */
    /* ====================================================== */
    private void openEditor(Course course) {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");
        dialog.setHeaderTitle("Editar curso");

        Binder<Course> binder = new Binder<>(Course.class);
        TextField      nameField = new TextField("Nombre");
        Select<Professor> profSelect = new Select<>();

        binder.forField(nameField).asRequired("El nombre es obligatorio")
              .bind(Course::getName, Course::setName);

        profSelect.setLabel("Profesor");
        profSelect.setItems(profService.findAll());
        profSelect.setItemLabelGenerator(Professor::getName);
        binder.forField(profSelect)
              .bind(Course::getProfessor, Course::setProfessor);

        binder.readBean(course);

        Button save   = new Button("Guardar",
                        e -> { if (binder.writeBeanIfValid(course)) {
                                   courseService.save(course);
                                   applyFilter(filter.getValue());
                                   dialog.close();
                               }});
        Button cancel = new Button("Cerrar", e -> dialog.close());

        dialog.add(new VerticalLayout(
            nameField, profSelect,
            new HorizontalLayout(save, cancel)
        ));
        dialog.open();
    }

    /* ====================================================== */
    /* Diálogo de inscriptos                                  */
    /* ====================================================== */
    private void openSeatsDialog(Course course) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Curso: " + course.getName());
        d.setWidth("50vw");
        d.setHeight("50vh");

        String profesor = course.getProfessor() != null
                          ? course.getProfessor().getName()
                          : "(sin profesor)";
        Span profLabel = new Span("Profesor: " + profesor);
        profLabel.getStyle().set("font-weight", "600")
                            .set("margin-bottom", "var(--lumo-space-s)");

        Grid<Seat> seatsGrid = new Grid<>(Seat.class, false);
        seatsGrid.addColumn(s -> s.getStudent().getName())
                 .setHeader("Alumno");
        seatsGrid.addColumn(s -> s.getMark() != null ? s.getMark() : "-")
                 .setHeader("Nota").setWidth("120px");
        seatsGrid.setItems(seatService.findByCourseId(course.getId()));
        seatsGrid.setSizeFull();

        Button editBtn  = new Button("Editar curso",
                           e -> { d.close(); openEditor(course); });
        Button closeBtn = new Button("Cerrar", e -> d.close());
        HorizontalLayout footer = new HorizontalLayout(editBtn, closeBtn);
        footer.getStyle().set("margin-top", "var(--lumo-space-m)");

        VerticalLayout wrapper = new VerticalLayout(profLabel, seatsGrid);
        wrapper.setSizeFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.expand(seatsGrid);

        d.add(wrapper);
        d.getFooter().add(footer);
        d.open();
    }

    /* ====================================================== */
    /* Confirmación de borrado                                */
    /* ====================================================== */
    private void confirmDelete(Course course) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Eliminar curso");
        dialog.setText("¿Estás seguro de que deseas eliminar “"
                       + course.getName() + "”?");

        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Eliminar");
        dialog.addConfirmListener(e -> {
            courseService.deleteById(course.getId());
            applyFilter(filter.getValue());
        });
        dialog.open();
    }
}
