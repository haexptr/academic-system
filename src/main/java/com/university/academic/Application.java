package com.university.academic;

import com.vaadin.flow.component.page.AppShellConfigurator;
// import com.vaadin.flow.theme.Theme; // Hapus import ini
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
// @Theme("academic") // Comment atau hapus baris ini
public class Application implements AppShellConfigurator {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}