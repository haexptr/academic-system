// MataKuliah.java
package com.university.academic.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "mata_kuliah")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MataKuliah {

    @Id
    @EqualsAndHashCode.Include
    @NotBlank(message = "Kode mata kuliah tidak boleh kosong")
    @Column(name = "kode_mk", length = 10)
    private String kodeMK;

    @NotBlank(message = "Nama mata kuliah tidak boleh kosong")
    @Column(name = "nama_mk", nullable = false)
    private String namaMK;

    @Positive(message = "SKS harus positif")
    @Column(nullable = false)
    private Integer sks;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private String fakultas;

    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "prerequisite")
    private String prerequisite; // Kode MK prerequisite, dipisah koma

    @OneToMany(mappedBy = "mataKuliah", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Nilai> daftarNilai;

    @OneToMany(mappedBy = "mataKuliah", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Jadwal> jadwalKuliah;

    // Method tambahan untuk mendapatkan prerequisite sebagai List
    // BUKAN override getter Lombok
    public List<String> getPrerequisiteList() {
        if (prerequisite != null && !prerequisite.trim().isEmpty()) {
            return List.of(prerequisite.split(","));
        }
        return List.of();
    }

    @Override
    public String toString() {
        return kodeMK + " - " + namaMK + " (" + sks + " SKS)";
    }
}