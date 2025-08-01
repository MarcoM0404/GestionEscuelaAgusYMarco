package com.example.app.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.example.app.base.ui.view.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonSecurityConfig extends VaadinWebSecurity {

    private final DbUserDetailsService uds;
    private final PasswordEncoder encoder;
    private final VaadinAuthSuccessHandler successHandler;

    public CommonSecurityConfig(
            DbUserDetailsService uds,
            PasswordEncoder encoder,
            VaadinAuthSuccessHandler successHandler) {
        this.uds = uds;
        this.encoder = encoder;
        this.successHandler = successHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/perform-logout").permitAll()
                .requestMatchers("/admin/**").hasRole(AppRoles.ADMIN.name())
                .requestMatchers("/professor/**").hasRole(AppRoles.PROFESSOR.name())
                .requestMatchers("/student/**").hasRole(AppRoles.STUDENT.name())
        );

        setLoginView(http, LoginView.class);

        http.formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .permitAll()
        );

        // Dejás el provider custom
        http.authenticationProvider(authenticationProvider());

        super.configure(http);
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }
}
	