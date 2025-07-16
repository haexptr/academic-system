package com.university.academic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoPasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Untuk demo UAS, gunakan BCrypt tapi dengan strength rendah (lebih cepat)
        return new BCryptPasswordEncoder(4); // Default: 10, Demo: 4 (lebih cepat)
    }

    // Method helper untuk generate demo passwords
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder(4);

        System.out.println("=== DEMO PASSWORDS FOR UAS ===");
        System.out.println("All passwords: demo123");
        System.out.println();

        String demoPassword = "demo123";
        String hash = encoder.encode(demoPassword);

        System.out.println("Password: " + demoPassword);
        System.out.println("Hash: " + hash);
        System.out.println();

        System.out.println("=== SQL FOR DEMO ===");
        System.out.println("UPDATE person SET password = '" + hash + "' WHERE email = 'admin@university.ac.id';");
        System.out.println("UPDATE person SET password = '" + hash + "' WHERE email = 'staff@university.ac.id';");
        System.out.println("UPDATE person SET password = '" + hash + "' WHERE email = 'john.doe@university.ac.id';");
        System.out.println("UPDATE person SET password = '" + hash + "' WHERE email = 'jane.smith@student.university.ac.id';");
        System.out.println();

        System.out.println("=== DEMO CREDENTIALS ===");
        System.out.println("Admin: admin@university.ac.id / demo123");
        System.out.println("Staff: staff@university.ac.id / demo123");
        System.out.println("Dosen: john.doe@university.ac.id / demo123");
        System.out.println("Mahasiswa: jane.smith@student.university.ac.id / demo123");
    }
}