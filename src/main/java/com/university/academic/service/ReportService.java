// ReportService.java
package com.university.academic.service;

import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.entity.Nilai;
import com.university.academic.model.entity.MataKuliah;
import com.university.academic.repository.MahasiswaRepository;
import com.university.academic.repository.NilaiRepository;
import com.university.academic.repository.MataKuliahRepository;
import com.university.academic.repository.DosenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MahasiswaRepository mahasiswaRepository;
    private final NilaiRepository nilaiRepository;
    private final MataKuliahRepository mataKuliahRepository;
    private final DosenRepository dosenRepository; // TAMBAHAN INI

    public Map<String, Object> generateTranskrip(String nim) {
        Map<String, Object> transkrip = new HashMap<>();

        Mahasiswa mahasiswa = mahasiswaRepository.findByNim(nim)
                .orElseThrow(() -> new RuntimeException("Mahasiswa tidak ditemukan"));

        List<Nilai> daftarNilai = nilaiRepository.findByMahasiswa(mahasiswa);

        transkrip.put("mahasiswa", mahasiswa);
        transkrip.put("daftarNilai", daftarNilai);
        transkrip.put("totalSKS", daftarNilai.stream()
                .mapToInt(n -> n.getMataKuliah().getSks())
                .sum());
        transkrip.put("ipk", mahasiswa.getIpk());

        return transkrip;
    }

    public Map<String, Object> generateLaporanNilaiMataKuliah(String kodeMK) {
        Map<String, Object> laporan = new HashMap<>();

        MataKuliah mataKuliah = mataKuliahRepository.findById(kodeMK)
                .orElseThrow(() -> new RuntimeException("Mata kuliah tidak ditemukan"));

        List<Nilai> daftarNilai = nilaiRepository.findByMataKuliah(mataKuliah);

        laporan.put("mataKuliah", mataKuliah);
        laporan.put("daftarNilai", daftarNilai);
        laporan.put("jumlahMahasiswa", daftarNilai.size());

        if (!daftarNilai.isEmpty()) {
            double rataRata = daftarNilai.stream()
                    .filter(n -> n.getNilaiAngka() != null)
                    .mapToDouble(n -> n.getNilaiAngka().doubleValue())
                    .average()
                    .orElse(0.0);
            laporan.put("rataRata", rataRata);
        }

        return laporan;
    }

    public Map<String, Long> getStatistikMahasiswaByJurusan() {
        List<Mahasiswa> allMahasiswa = mahasiswaRepository.findAll();

        return allMahasiswa.stream()
                .collect(Collectors.groupingBy(
                        Mahasiswa::getJurusan,
                        Collectors.counting()));
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalMahasiswa", mahasiswaRepository.count());
        stats.put("totalMataKuliah", mataKuliahRepository.count());
        stats.put("totalDosen", dosenRepository.count()); // TAMBAHAN INI - YANG HILANG!
        stats.put("statistikJurusan", getStatistikMahasiswaByJurusan());

        return stats;
    }
}