// Staff.java
package com.university.academic.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "staff")
@Data
@EqualsAndHashCode(callSuper = true)
public class Staff extends Person {

    @NotBlank(message = "NIP tidak boleh kosong")
    @Column(nullable = false, unique = true, length = 20)
    private String nip;

    @Column(nullable = false)
    private String departemen;

    @Column(nullable = false)
    private String jabatan;

    @Column(name = "status_kerja", nullable = false)
    private String statusKerja = "AKTIF";

    @Override
    public String getDisplayInfo() {
        return String.format("%s - %s (%s)", nip, getNama(), departemen);
    }

    public boolean hasWewenang(String operasi) {
        // Implementasi logic wewenang berdasarkan jabatan
        return "ADMIN".equals(jabatan) || "KEPALA_TU".equals(jabatan);
    }
}