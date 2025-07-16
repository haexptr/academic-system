// MataKuliahRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.MataKuliah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MataKuliahRepository extends JpaRepository<MataKuliah, String> {
    List<MataKuliah> findByFakultas(String fakultas);
    List<MataKuliah> findBySemester(Integer semester);
    List<MataKuliah> findBySks(Integer sks);

    @Query("SELECT mk FROM MataKuliah mk WHERE mk.namaMK LIKE %:nama% OR mk.kodeMK LIKE %:kode%")
    List<MataKuliah> findByNamaOrKodeContaining(@Param("nama") String nama, @Param("kode") String kode);

    @Query("SELECT mk FROM MataKuliah mk WHERE mk.fakultas = :fakultas AND mk.semester = :semester")
    List<MataKuliah> findByFakultasAndSemester(@Param("fakultas") String fakultas, @Param("semester") Integer semester);
}