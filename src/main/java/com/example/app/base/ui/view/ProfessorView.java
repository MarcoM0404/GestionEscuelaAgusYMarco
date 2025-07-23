package com.example.app.base.ui.view;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.OptionalDouble;

@Route(value = "professor", layout = MainLayout.class)
public class ProfessorView extends VerticalLayout {

	private final StudentService studentService;
	private final SeatService seatService;
	private final Grid<Course> courseGrid = new Grid<>(Course.class, false);

	@Autowired
	public ProfessorView(ProfessorService profService, StudentService studentService, CourseService courseService,
			SeatService seatService) {

		this.studentService = studentService;
		this.seatService = seatService;

		User u = VaadinSession.getCurrent().getAttribute(User.class);
		if (u == null || u.getRole() != Role.PROFESSOR) {
			UI.getCurrent().navigate("login");
			return;
		}

		setSizeFull();

		H2 welcome = new H2();
		welcome.add(new Icon(VaadinIcon.USER), new Text(" Bienvenido, " + u.getUsername()));
		add(welcome);

		courseGrid.addColumn(Course::getId).setHeader("ID").setWidth("70px");
		courseGrid.addColumn(Course::getName).setHeader("Curso").setAutoWidth(true);
		courseGrid.addColumn(c -> seatService.findByCourseId(c.getId()).size()).setHeader("Inscritos")
				.setAutoWidth(true);
		courseGrid.addColumn(c -> {
			OptionalDouble avgOpt = seatService.findByCourseId(c.getId()).stream()
					.mapToDouble(s -> s.getMark() != null ? s.getMark() : 0.0).average();
			return avgOpt.isPresent() ? String.format("%.2f", avgOpt.getAsDouble()) : "—";
		}).setHeader("Prom. Nota").setAutoWidth(true);

		courseGrid.asSingleSelect().addValueChangeListener(evt -> {
			if (evt.getValue() != null) {
				openEnrollmentDialog(evt.getValue());
			}
		});

		courseGrid.setSizeFull();

		H2 coursesHeader = new H2();
		coursesHeader.add(new Icon(VaadinIcon.BOOK), new Text(" Mis Cursos"));
		add(coursesHeader, courseGrid);

		Long profId = profService.findByUserId(u.getId()).map(p -> p.getId()).orElse(-1L);
		courseGrid.setItems(courseService.findByProfessorId(profId));
	}

	private void openEnrollmentDialog(Course course) {
		Dialog dialog = new Dialog();
		dialog.setWidth("70%");
		dialog.setHeight("80vh");

		H2 title = new H2();
		title.add(new Icon(VaadinIcon.CLIPBOARD_TEXT), new Span(" Inscripciones: " + course.getName()));
		dialog.add(title);

		final Student[] selected = new Student[1];

		Button chooseStudentBtn = new Button("Seleccionar alumno", new Icon(VaadinIcon.SEARCH));
		chooseStudentBtn.addClickListener(e -> openStudentPicker(course, selected, chooseStudentBtn));

		DatePicker inscDate = new DatePicker("Fecha Inscripción");
		inscDate.setValue(java.time.LocalDate.now());

		Button enrollBtn = new Button("Inscribir", new Icon(VaadinIcon.PLUS_CIRCLE));
		enrollBtn.addClickListener(ev -> {
			Student s = selected[0];
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
			selected[0] = null;
			chooseStudentBtn.setText("Seleccionar alumno");
		});

		HorizontalLayout toolbar = new HorizontalLayout(chooseStudentBtn, inscDate, enrollBtn);
		toolbar.setWidthFull();
		toolbar.setSpacing(true);
		toolbar.setAlignItems(Alignment.END);
		toolbar.expand(inscDate);

		Grid<Seat> seatGrid = new Grid<>(Seat.class, false);
		seatGrid.addColumn(Seat::getId).setHeader("ID").setWidth("50px");
		seatGrid.addColumn(s -> s.getStudent().getName()).setHeader("Alumno").setAutoWidth(true);
		seatGrid.addColumn(Seat::getYear).setHeader("Inscripción");
		seatGrid.addColumn(Seat::getEvaluationDate).setHeader("Evaluación");
		seatGrid.addColumn(Seat::getMark).setHeader("Nota");
		seatGrid.addComponentColumn(seat -> {
			Icon trash = new Icon(VaadinIcon.TRASH);
			Button del = new Button(trash);
			del.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
			del.addClickListener(ev -> {
				seatService.deleteById(seat.getId());
				refreshSeatGrid(course, dialog);
			});
			return del;
		}).setHeader("Quitar");
		seatGrid.asSingleSelect().addValueChangeListener(evt -> {
			if (evt.getValue() != null)
				openEditSeatDialog(evt.getValue(), seatGrid);
		});

		seatGrid.setSizeFull();

		dialog.add(toolbar, seatGrid);
		dialog.open();
		refreshSeatGrid(course, dialog);
	}

	private void openStudentPicker(Course course, Student[] selected, Button chooseStudentBtn) {

		Dialog picker = new Dialog();
		picker.setWidth("450px");
		picker.setModal(true);

		TextField filterField = new TextField();
		filterField.setPlaceholder("Buscar alumno…");
		filterField.setClearButtonVisible(true);
		filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		filterField.setWidthFull();

		Grid<Student> grid = new Grid<>(Student.class, false);
		grid.addColumn(Student::getId).setHeader("ID").setWidth("70px");
		grid.addColumn(Student::getName).setHeader("Nombre").setAutoWidth(true);
		grid.addColumn(s -> s.getStudentNumber().toString())
	    .setHeader("Matrícula")
	    .setAutoWidth(true);


		List<Long> already = seatService.findByCourseId(course.getId()).stream().map(seat -> seat.getStudent().getId())
				.toList();

		List<Student> candidates = studentService.findAll().stream().filter(st -> !already.contains(st.getId()))
				.toList();

		ListDataProvider<Student> provider = new ListDataProvider<>(candidates);
		grid.setDataProvider(provider);

		filterField.addValueChangeListener(e -> {
			String term = e.getValue().trim().toLowerCase();
			provider.setFilter(stu -> term.isEmpty() || stu.getName().toLowerCase().contains(term)
					|| String.valueOf(stu.getId()).contains(term));
		});

		grid.addItemDoubleClickListener(ev -> {
			selected[0] = ev.getItem();
			chooseStudentBtn.setText("Alumno: " + selected[0].getName());
			picker.close();
		});

		VerticalLayout content = new VerticalLayout(filterField, grid);
		content.setPadding(false);
		content.setSpacing(false);
		content.setSizeFull();

		picker.add(content);
		picker.open();
	}

	@SuppressWarnings("unchecked")
	private void refreshSeatGrid(Course course, Dialog dialog) {
		dialog.getChildren().filter(c -> c instanceof Grid).map(c -> (Grid<Seat>) c).findFirst()
				.ifPresent(g -> g.setItems(seatService.findByCourseId(course.getId())));
	}

	private void openEditSeatDialog(Seat seat, Grid<Seat> seatGrid) {
	    Dialog d = new Dialog();
	    d.setWidth("400px");

	    H2 editTitle = new H2();
	    editTitle.add(new Icon(VaadinIcon.EDIT), new Text(" Editar Inscripción"));
	    d.add(editTitle);

	    com.vaadin.flow.component.textfield.NumberField markField =
	        new com.vaadin.flow.component.textfield.NumberField("Nota");
	    markField.setPrefixComponent(new Icon(VaadinIcon.STAR));
	    DatePicker evalDate = new DatePicker("Fecha Evaluación");
	    markField.setValue(seat.getMark());
	    evalDate.setValue(seat.getEvaluationDate());

	    Button save = new Button("Guardar", e -> {
	        seat.setMark(markField.getValue());
	        seat.setEvaluationDate(evalDate.getValue());
	        seatService.save(seat);
	        seatGrid.getDataProvider().refreshAll();
	        d.close();
	    });
	    save.setIcon(new Icon(VaadinIcon.CHECK));

	    Button cancel = new Button("Cancelar", e -> d.close());
	    cancel.setIcon(new Icon(VaadinIcon.CLOSE));

	    d.add(new VerticalLayout(markField, evalDate, new HorizontalLayout(save, cancel)));
	    d.open();  // ← ¡AHÍ VA EL PUNTO Y COMA!

	} 

}