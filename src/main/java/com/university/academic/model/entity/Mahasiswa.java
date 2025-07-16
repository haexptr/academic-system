// Mahasiswa.java
package com.university.academic.model.entity;

import com.university.academic.model.enums.StatusMahasiswa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "mahasiswa")
@Data
@EqualsAndHashCode(callSuper = true)
public class Mahasiswa extends Person {

    @NotBlank(message = "NIM tidak boleh kosong")
    @Column(nullable = false, unique = true, length = 20)
    private String nim;

    @NotBlank(message = "Jurusan tidak boleh kosong")
    @Column(nullable = false)
    private String jurusan;

    @Column(nullable = false)
    private Integer semester = 1;

    @Column(precision = 3, scale = 2)
    private BigDecimal ipk = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_mahasiswa", nullable = false)
    private StatusMahasiswa statusMahasiswa = StatusMahasiswa.AKTIF;

    @OneToMany(mappedBy = "mahasiswa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Nilai> daftarNilai;

    @Override
    public String getDisplayInfo() {
        return String.format("%s - %s (%s)", nim, getNama(), jurusan);
    }

    public void hitungIPK() {
        if (daftarNilai != null && !daftarNilai.isEmpty()) {
            double totalNilai = daftarNilai.stream()
                    .mapToDouble(nilai -> nilai.getNilaiAngka().doubleValue() * nilai.getMataKuliah().getSks())
                    .sum();

            int totalSKS = daftarNilai.stream()
                    .mapToInt(nilai -> nilai.getMataKuliah().getSks())
                    .sum();

            if (totalSKS > 0) {
                this.ipk = BigDecimal.valueOf(totalNilai / totalSKS).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    public void updateSemester() {
        this.semester++;
    }
}