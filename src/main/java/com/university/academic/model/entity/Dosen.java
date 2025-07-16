package com.university.academic.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "dosen")
@Data
@EqualsAndHashCode(callSuper = true)
public class Dosen extends Person {

    @NotBlank(message = "NIP tidak boleh kosong")
    @Column(nullable = false, unique = true, length = 20)
    private String nip;

    @Column(nullable = false)
    private String jabatan;

    @Column(nullable = false)
    private String fakultas;

    @Column(name = "bidang_keahlian")
    private String bidangKeahlian;

    @Column(name = "status_dosen", nullable = false)
    private String statusDosen = "AKTIF";

    @OneToMany(mappedBy = "dosen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Jadwal> jadwalMengajar;

    @OneToMany(mappedBy = "dosen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Nilai> nilaiDiajar;

    @Override
    public String getDisplayInfo() {
        return String.format("%s - %s (%s)", nip, getNama(), jabatan);
    }

    public List<MataKuliah> getMataKuliahDiajar() {
        return jadwalMengajar.stream()
                .map(Jadwal::getMataKuliah)
                .distinct()
                .toList();
    }
}