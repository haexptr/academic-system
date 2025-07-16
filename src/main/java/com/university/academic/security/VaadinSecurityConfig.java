package com.university.academic.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import com.university.academic.views.LoginView;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class VaadinSecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Ini akan mengatur login route Vaadin
        super.configure(http);
        setLoginView(http, LoginView.class);
    }
}
