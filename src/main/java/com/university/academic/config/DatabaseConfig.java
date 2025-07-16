// DatabaseConfig.java
package com.university.academic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.university.academic.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Konfigurasi database sudah ada di application.properties
}