// NilaiRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Nilai;
import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.entity.MataKuliah;
import com.university.academic.model.entity.Dosen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NilaiRepository extends JpaRepository<Nilai, Long> {
    List<Nilai> findByMahasiswa(Mahasiswa mahasiswa);
    List<Nilai> findByMataKuliah(MataKuliah mataKuliah);
    List<Nilai> findByDosen(Dosen dosen);
    List<Nilai> findByMahasiswaAndSemester(Mahasiswa mahasiswa, Integer semester);
    List<Nilai> findByTahunAkademik(String tahunAkademik);

    Optional<Nilai> findByMahasiswaAndMataKuliah(Mahasiswa mahasiswa, MataKuliah mataKuliah);

    // TAMBAHAN BARU - Query dengan JOIN FETCH untuk mengatasi LazyInitializationException
    @Query("SELECT n FROM Nilai n " +
            "LEFT JOIN FETCH n.mahasiswa " +
            "LEFT JOIN FETCH n.mataKuliah " +
            "LEFT JOIN FETCH n.dosen")
    List<Nilai> findAllWithMahasiswaMataKuliahAndDosen();

    @Query("SELECT n FROM Nilai n " +
            "LEFT JOIN FETCH n.mahasiswa " +
            "LEFT JOIN FETCH n.mataKuliah " +
            "LEFT JOIN FETCH n.dosen " +
            "WHERE n.mahasiswa = :mahasiswa")
    List<Nilai> findByMahasiswaWithDetails(@Param("mahasiswa") Mahasiswa mahasiswa);

    @Query("SELECT n FROM Nilai n " +
            "LEFT JOIN FETCH n.mahasiswa " +
            "LEFT JOIN FETCH n.mataKuliah " +
            "LEFT JOIN FETCH n.dosen " +
            "WHERE n.dosen = :dosen")
    List<Nilai> findByDosenWithDetails(@Param("dosen") Dosen dosen);

    @Query("SELECT n FROM Nilai n " +
            "LEFT JOIN FETCH n.mahasiswa " +
            "LEFT JOIN FETCH n.mataKuliah " +
            "LEFT JOIN FETCH n.dosen " +
            "WHERE n.mataKuliah = :mataKuliah")
    List<Nilai> findByMataKuliahWithDetails(@Param("mataKuliah") MataKuliah mataKuliah);

    @Query("SELECT n FROM Nilai n WHERE n.mahasiswa.nim = :nim AND n.semester = :semester")
    List<Nilai> findByNimAndSemester(@Param("nim") String nim, @Param("semester") Integer semester);

    @Query("SELECT AVG(n.nilaiAngka) FROM Nilai n WHERE n.mataKuliah = :mataKuliah")
    Double getAverageNilaiByMataKuliah(@Param("mataKuliah") MataKuliah mataKuliah);
}