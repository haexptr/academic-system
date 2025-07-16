// AcademicService.java
package com.university.academic.service;

import com.university.academic.model.entity.*;
import com.university.academic.model.enums.Hari;
import com.university.academic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AcademicService {

    private final MataKuliahRepository mataKuliahRepository;
    private final NilaiRepository nilaiRepository;
    private final JadwalRepository jadwalRepository;
    private final MahasiswaRepository mahasiswaRepository;

    // Mata Kuliah Management
    public List<MataKuliah> findAllMataKuliah() {
        return mataKuliahRepository.findAll();
    }

    public Optional<MataKuliah> findMataKuliahById(String kodeMK) {
        return mataKuliahRepository.findById(kodeMK);
    }

    public MataKuliah saveMataKuliah(MataKuliah mataKuliah) {
        return mataKuliahRepository.save(mataKuliah);
    }

    public void deleteMataKuliah(String kodeMK) {
        mataKuliahRepository.deleteById(kodeMK);
    }

    public List<MataKuliah> findMataKuliahByFakultas(String fakultas) {
        return mataKuliahRepository.findByFakultas(fakultas);
    }

    public List<MataKuliah> findMataKuliahBySemester(Integer semester) {
        return mataKuliahRepository.findBySemester(semester);
    }

    // Nilai Management - DIPERBAIKI
    public List<Nilai> findAllNilai() {
        return nilaiRepository.findAllWithMahasiswaMataKuliahAndDosen(); // DIUBAH dari findAll()
    }

    public Nilai saveNilai(Nilai nilai) {
        nilai.hitungNilaiAkhir();
        Nilai savedNilai = nilaiRepository.save(nilai);

        // Update IPK mahasiswa
        updateMahasiswaIPK(nilai.getMahasiswa());

        return savedNilai;
    }

    public List<Nilai> findNilaiByMahasiswa(Mahasiswa mahasiswa) {
        return nilaiRepository.findByMahasiswaWithDetails(mahasiswa); // DIUBAH dari findByMahasiswa()
    }

    public List<Nilai> findNilaiByDosen(Dosen dosen) {
        return nilaiRepository.findByDosenWithDetails(dosen); // DIUBAH dari findByDosen()
    }

    public List<Nilai> findNilaiByMataKuliah(MataKuliah mataKuliah) {
        return nilaiRepository.findByMataKuliahWithDetails(mataKuliah); // DIUBAH dari findByMataKuliah()
    }

    public Optional<Nilai> findNilaiByMahasiswaAndMataKuliah(Mahasiswa mahasiswa, MataKuliah mataKuliah) {
        return nilaiRepository.findByMahasiswaAndMataKuliah(mahasiswa, mataKuliah);
    }

    // DIPERBAIKI - Method konversi skala 100 ke skala 4.0
    private double konversiKeSkala4(double nilaiAngka) {
        if (nilaiAngka >= 85) return 4.0;      // A
        else if (nilaiAngka >= 80) return 3.7; // A-
        else if (nilaiAngka >= 75) return 3.3; // B+
        else if (nilaiAngka >= 70) return 3.0; // B
        else if (nilaiAngka >= 65) return 2.7; // B-
        else if (nilaiAngka >= 60) return 2.3; // C+
        else if (nilaiAngka >= 55) return 2.0; // C
        else if (nilaiAngka >= 50) return 1.7; // C-
        else if (nilaiAngka >= 45) return 1.3; // D+
        else if (nilaiAngka >= 40) return 1.0; // D
        else return 0.0; // E/F
    }

    // DIPERBAIKI - Method updateMahasiswaIPK
    private void updateMahasiswaIPK(Mahasiswa mahasiswa) {
        List<Nilai> daftarNilai = nilaiRepository.findByMahasiswaWithDetails(mahasiswa); // DIUBAH

        if (!daftarNilai.isEmpty()) {
            double totalNilai = 0;
            int totalSKS = 0;

            for (Nilai nilai : daftarNilai) {
                if (nilai.getNilaiAngka() != null) {
                    // Konversi nilai angka (0-100) ke skala 4.0
                    double nilaiAngka = nilai.getNilaiAngka().doubleValue();
                    double nilaiSkala4 = konversiKeSkala4(nilaiAngka);

                    totalNilai += nilaiSkala4 * nilai.getMataKuliah().getSks();
                    totalSKS += nilai.getMataKuliah().getSks();
                }
            }

            if (totalSKS > 0) {
                double ipkValue = totalNilai / totalSKS;

                // Pastikan IPK dalam range 0.00 - 4.00
                if (ipkValue > 4.0) ipkValue = 4.0;
                if (ipkValue < 0.0) ipkValue = 0.0;

                BigDecimal ipk = BigDecimal.valueOf(ipkValue)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                mahasiswa.setIpk(ipk);
                mahasiswaRepository.save(mahasiswa);
            }
        }
    }

    // Jadwal Management - DIPERBAIKI
    public List<Jadwal> findAllJadwal() {
        return jadwalRepository.findAllWithMataKuliahAndDosen(); // DIUBAH dari findAll()
    }

    // TAMBAHAN BARU - Method dengan JOIN FETCH untuk optimasi
    public List<Jadwal> findJadwalByDosenWithDetails(Dosen dosen) {
        return jadwalRepository.findByDosenWithMataKuliah(dosen);
    }

    public List<Jadwal> findJadwalByHariWithDetails(Hari hari) {
        return jadwalRepository.findByHariWithMataKuliahAndDosen(hari);
    }

    public Jadwal saveJadwal(Jadwal jadwal) throws Exception {
        // Cek konflik jadwal
        List<Jadwal> conflicts = jadwalRepository.findConflictingSchedules(
                jadwal.getHari(), jadwal.getRuangan(),
                jadwal.getJamMulai(), jadwal.getJamSelesai());

        if (!conflicts.isEmpty()) {
            throw new Exception("Konflik jadwal di ruangan " + jadwal.getRuangan() +
                    " pada " + jadwal.getHari().getDisplayName() +
                    " jam " + jadwal.getJamMulai() + "-" + jadwal.getJamSelesai());
        }

        // Cek konflik dosen
        List<Jadwal> dosenConflicts = jadwalRepository.findDosenConflictingSchedules(
                jadwal.getDosen(), jadwal.getHari(),
                jadwal.getJamMulai(), jadwal.getJamSelesai());

        if (!dosenConflicts.isEmpty()) {
            throw new Exception("Dosen " + jadwal.getDosen().getNama() +
                    " sudah memiliki jadwal pada waktu tersebut");
        }

        return jadwalRepository.save(jadwal);
    }

    public List<Jadwal> findJadwalByDosen(Dosen dosen) {
        return jadwalRepository.findByDosen(dosen);
    }

    public void deleteJadwal(Long id) {
        jadwalRepository.deleteById(id);
    }

    // Transkrip Nilai
    public List<Nilai> getTranskripNilai(String nim) {
        Optional<Mahasiswa> mahasiswaOpt = mahasiswaRepository.findByNim(nim);
        if (mahasiswaOpt.isPresent()) {
            return nilaiRepository.findByMahasiswaWithDetails(mahasiswaOpt.get()); // DIUBAH
        }
        return List.of();
    }

    // Statistik
    public Double getAverageNilaiMataKuliah(String kodeMK) {
        Optional<MataKuliah> mkOpt = mataKuliahRepository.findById(kodeMK);
        if (mkOpt.isPresent()) {
            return nilaiRepository.getAverageNilaiByMataKuliah(mkOpt.get());
        }
        return 0.0;
    }

    // Method untuk delete nilai - TAMBAHAN BARU
    public void deleteNilaiById(Long id) {
        // Ambil data nilai dulu untuk update IPK setelah delete
        Optional<Nilai> nilaiOpt = nilaiRepository.findById(id);
        if (nilaiOpt.isPresent()) {
            Mahasiswa mahasiswa = nilaiOpt.get().getMahasiswa();
            nilaiRepository.deleteById(id);

            // Update IPK mahasiswa setelah nilai dihapus
            updateMahasiswaIPK(mahasiswa);
        }
    }
}