// Person.java
package com.university.academic.model.entity;

import com.university.academic.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {  // HAPUS 'abstract'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Nama tidak boleh kosong")
    @Column(nullable = false)
    private String nama;

    @Email(message = "Format email tidak valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 15)
    private String telepon;

    @Column(columnDefinition = "TEXT")
    private String alamat;

    @Column(name = "tanggal_lahir")
    private LocalDate tanggalLahir;

    @Column(name = "jenis_kelamin", length = 1)
    private String jenisKelamin; // L/P

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotBlank(message = "Password tidak boleh kosong")
    @Column(nullable = false)
    private String password;

    // Implementasi method (bukan abstract lagi)
    public String getDisplayInfo() {
        return nama + " (" + email + ")";
    }

    @Override
    public String toString() {
        return nama + " (" + email + ")";
    }
}