// Nilai.java
package com.university.academic.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "nilai")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Nilai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private Mahasiswa mahasiswa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mata_kuliah_id", nullable = false)
    private MataKuliah mataKuliah;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosen_id", nullable = false)
    private Dosen dosen;

    @Column(name = "nilai_angka", precision = 5, scale = 2)
    private BigDecimal nilaiAngka;

    @Column(name = "nilai_huruf", length = 2)
    private String nilaiHuruf;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "tahun_akademik", length = 9)
    private String tahunAkademik; // Format: 2023/2024

    public void hitungNilaiAkhir() {
        // Implementasi perhitungan nilai akhir dari komponen nilai
        // Misalnya: UTS (30%) + UAS (40%) + Tugas (30%)
        // Untuk simplifikasi, nilai sudah final
        konversiNilai();
    }

    public void konversiNilai() {
        if (nilaiAngka != null) {
            double nilai = nilaiAngka.doubleValue();
            if (nilai >= 85) {
                nilaiHuruf = "A";
            } else if (nilai >= 80) {
                nilaiHuruf = "A-";
            } else if (nilai >= 75) {
                nilaiHuruf = "B+";
            } else if (nilai >= 70) {
                nilaiHuruf = "B";
            } else if (nilai >= 65) {
                nilaiHuruf = "B-";
            } else if (nilai >= 60) {
                nilaiHuruf = "C+";
            } else if (nilai >= 55) {
                nilaiHuruf = "C";
            } else if (nilai >= 50) {
                nilaiHuruf = "C-";
            } else if (nilai >= 45) {
                nilaiHuruf = "D";
            } else {
                nilaiHuruf = "E";
            }
        }
    }
}