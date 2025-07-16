// DosenRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Dosen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DosenRepository extends JpaRepository<Dosen, Long> {
    Optional<Dosen> findByNip(String nip);
    List<Dosen> findByFakultas(String fakultas);
    List<Dosen> findByJabatan(String jabatan);
    List<Dosen> findByStatusDosen(String statusDosen);
    boolean existsByNip(String nip);

    @Query("SELECT d FROM Dosen d WHERE d.nama LIKE %:nama% OR d.nip LIKE %:nip%")
    List<Dosen> findByNamaOrNipContaining(@Param("nama") String nama, @Param("nip") String nip);

    @Query("SELECT DISTINCT d FROM Dosen d JOIN d.jadwalMengajar j WHERE j.mataKuliah.kodeMK = :kodeMK")
    List<Dosen> findByMataKuliahDiajar(@Param("kodeMK") String kodeMK);
}