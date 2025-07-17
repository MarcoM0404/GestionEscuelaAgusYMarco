package com.example.app;

import com.example.app.base.domain.*;
import com.example.app.base.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner dataInitializer(
            UserService userService,
            PersonService personService,
            ProfessorService professorService,
            StudentService studentService,
            PasswordEncoder encoder) {
        return args -> {
            Map<String, Role> defaults = Map.of(
                "admin",    Role.ADMIN,
                "profesor", Role.PROFESSOR,
                "alumno",   Role.STUDENT
            );

            for (var entry : defaults.entrySet()) {
                String username = entry.getKey();
                Role role       = entry.getValue();

                User u = userService.findByUsername(username);
                if (u == null) {
                    u = new User(username,
                                 encoder.encode(username),
                                 role);
                } else {
                    u.setPassword(encoder.encode(username));
                    u.setRole(role);
                }
                userService.save(u);

                if (role == Role.PROFESSOR
                    && professorService.findByUserId(u.getId()).isEmpty()) {
                    Professor p = new Professor("Profesor " + username,
                                                username + "@example.com",
                                                0.0);
                    p.setUser(u);
                    professorService.save(p);
                }
                if (role == Role.STUDENT
                    && studentService.findByUserId(u.getId()).isEmpty()) {
                    Student s = new Student(UUID.randomUUID(), 0.0);
                    s.setName("Alumno " + username);
                    s.setEmail(username + "@example.com");
                    s.setUser(u);
                    studentService.save(s);
                }
            }
        };
    }
}