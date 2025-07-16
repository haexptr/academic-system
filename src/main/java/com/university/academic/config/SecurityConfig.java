package com.university.academic.config;

import com.university.academic.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationService authenticationService;

    // COMMENT SEMENTARA - pakai yang di DemoPasswordConfig
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Vaadin resources
                        .requestMatchers(
                                "/VAADIN/**",
                                "/vaadinServlet/**",
                                "/frontend/**",
                                "/sw.js",
                                "/offline.html",
                                "/icons/**",
                                "/images/**",
                                "/themes/**",
                                "/webjars/**"
                        ).permitAll()

                        // Public pages
                        .requestMatchers("/", "/login").permitAll()

                        // Role-based access
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/dosen/**").hasAnyRole("DOSEN", "ADMIN")
                        .requestMatchers("/mahasiswa/**").hasAnyRole("MAHASISWA", "ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)  // Redirect ke root setelah login
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(authenticationService)
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }
}