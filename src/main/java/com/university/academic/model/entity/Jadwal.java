// Jadwal.java
package com.university.academic.model.entity;

import com.university.academic.model.enums.Hari;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Entity
@Table(name = "jadwal")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Jadwal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mata_kuliah_id", nullable = false)
    private MataKuliah mataKuliah;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dosen_id", nullable = false)
    private Dosen dosen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hari hari;

    @Column(name = "jam_mulai", nullable = false)
    private LocalTime jamMulai;

    @Column(name = "jam_selesai", nullable = false)
    private LocalTime jamSelesai;

    @Column(nullable = false)
    private String ruangan;

    @Column(name = "tahun_akademik", length = 9)
    private String tahunAkademik;

    public boolean cekKonflik(Jadwal jadwalLain) {
        // Cek konflik hari yang sama
        if (!this.hari.equals(jadwalLain.getHari())) {
            return false;
        }

        // Cek konflik waktu
        LocalTime mulaiLain = jadwalLain.getJamMulai();
        LocalTime selesaiLain = jadwalLain.getJamSelesai();

        return !(this.jamSelesai.isBefore(mulaiLain) || this.jamMulai.isAfter(selesaiLain));
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s (%s)",
                mataKuliah.getNamaMK(),
                hari.getDisplayName(),
                jamMulai + "-" + jamSelesai,
                ruangan);
    }
}