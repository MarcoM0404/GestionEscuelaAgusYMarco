package com.example.app.base.ui.view;

import com.example.app.base.domain.Administrator;
import com.example.app.base.domain.User;
import com.example.app.base.service.AdministratorService;
import com.example.app.base.service.UserService;
import com.example.app.security.AppRoles;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@RolesAllowed("ADMIN")
@PageTitle("Administradores")
@Route(value = "admin/admins", layout = MainLayout.class)
public class AdminAdminsView extends VerticalLayout {

    private final UserService          userService;
    private final AdministratorService adminService;
    private final PasswordEncoder      encoder;

    private final Grid<Administrator> grid   = new Grid<>(Administrator.class, false);
    private final TextField           filter = new TextField();

    public AdminAdminsView(UserService userService,
                           AdministratorService adminService,
                           PasswordEncoder encoder) {
        this.userService  = userService;
        this.adminService = adminService;
        this.encoder      = encoder;

        
        User u = VaadinSession.getCurrent().getAttribute(User.class);
        if (u == null || u.getRole() != AppRoles.ADMIN) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        buildHeader();
        configureGrid();
        add(grid);
        applyFilter("");
    }

   

    private void buildHeader() {
        Icon adminIcon = VaadinIcon.USER_STAR.create();
        adminIcon.getStyle().set("margin-right", "4px");

        H2 titleText = new H2("Gestión de Administradores");
        HorizontalLayout title = new HorizontalLayout(adminIcon, titleText);
        title.setAlignItems(Alignment.CENTER);

        Icon plus = VaadinIcon.PLUS_CIRCLE.create();
        Button addBtn = new Button("Nuevo Admin", plus,
                e -> openEditor(new Administrator(), true));
        addBtn.setIconAfterText(false);

        filter.setPlaceholder("Buscar admin…");
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setClearButtonVisible(true);
        filter.addValueChangeListener(e -> applyFilter(e.getValue()));

        HorizontalLayout header = new HorizontalLayout(title, filter, addBtn);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        header.expand(title);
        add(header);
    }

    private void configureGrid() {
        grid.addColumn(a -> a.getUser().getId())
            .setHeader("ID").setWidth("70px");
        grid.addColumn(a -> a.getUser().getUsername())
            .setHeader("Usuario").setAutoWidth(true);
        grid.addColumn(Administrator::getName).setHeader("Nombre");
        grid.addColumn(Administrator::getEmail).setHeader("Email");

        grid.addComponentColumn(admin -> {
            Button trash = new Button(VaadinIcon.TRASH.create(), click -> {
                adminService.deleteById(admin.getId());
                applyFilter(filter.getValue());
                Notification.show("Administrador eliminado");
            });
            trash.addThemeVariants(ButtonVariant.LUMO_ERROR);
            return trash;
        }).setWidth("80px").setFlexGrow(0);

        grid.setSizeFull();
        grid.addItemDoubleClickListener(ev -> openEditor(ev.getItem(), false));
    }

    private void applyFilter(String term) {
        grid.setItems(term == null || term.isBlank()
            ? adminService.findAll()
            : adminService.search(term));
    }

    

    private void openEditor(Administrator admin, boolean isNew) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(null);
        dialog.setWidth("420px");

        Icon starAdmin = VaadinIcon.USER_STAR.create();
        starAdmin.getStyle().set("margin-right", "6px");
        H2 hdr = new H2((isNew ? "Nuevo" : "Editar") + " administrador");
        HorizontalLayout hdrLayout = new HorizontalLayout(starAdmin, hdr);
        hdrLayout.setAlignItems(Alignment.CENTER);
        dialog.getHeader().add(hdrLayout);

        Binder<Administrator> binder = new Binder<>(Administrator.class);

        TextField usern = new TextField("Usuario");
        usern.setPrefixComponent(VaadinIcon.USER.create());

        PasswordField pass = new PasswordField("Contraseña");
        pass.setPrefixComponent(VaadinIcon.LOCK.create());

        TextField name = new TextField("Nombre");
        name.setPrefixComponent(VaadinIcon.USER.create());

        TextField email = new TextField("Email");
        email.setPrefixComponent(VaadinIcon.ENVELOPE.create());

        binder.forField(name).asRequired().bind(Administrator::getName, Administrator::setName);
        binder.forField(email).asRequired().bind(Administrator::getEmail, Administrator::setEmail);

        if (!isNew) {
            usern.setValue(admin.getUser().getUsername());
            usern.setReadOnly(true);
            pass.setPlaceholder("Dejar vacío para no cambiar");
        }

        binder.readBean(admin);

        Button save = new Button("Guardar", e -> {
            if (binder.writeBeanIfValid(admin)) {

                User user;
                if (isNew) {
                    if (userService.existsByUsername(usern.getValue())) {
                        usern.setInvalid(true);
                        usern.setErrorMessage("Usuario ya existe");
                        return;
                    }
                    user = new User(
                            usern.getValue(),
                            encoder.encode(pass.getValue().isBlank()
                                           ? usern.getValue()
                                           : pass.getValue()),
                            AppRoles.ADMIN
                    );
                    userService.save(user);
                    admin.setUser(user);
                } else {
                    user = admin.getUser();
                    if (!pass.getValue().isBlank()) {
                        user.setPassword(encoder.encode(pass.getValue()));
                        userService.save(user);
                    }
                }

                adminService.save(admin);
                applyFilter(filter.getValue());
                dialog.close();
            }
        });
        Button cancel = new Button("Cerrar", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(new VerticalLayout(usern, pass, name, email, actions));
        dialog.open();
    }
}