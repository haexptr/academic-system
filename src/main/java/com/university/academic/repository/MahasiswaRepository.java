// MahasiswaRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.enums.StatusMahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MahasiswaRepository extends JpaRepository<Mahasiswa, Long> {
    Optional<Mahasiswa> findByNim(String nim);
    List<Mahasiswa> findByJurusan(String jurusan);
    List<Mahasiswa> findByStatusMahasiswa(StatusMahasiswa status);
    List<Mahasiswa> findBySemester(Integer semester);
    boolean existsByNim(String nim);

    @Query("SELECT m FROM Mahasiswa m WHERE m.nama LIKE %:nama% OR m.nim LIKE %:nim%")
    List<Mahasiswa> findByNamaOrNimContaining(@Param("nama") String nama, @Param("nim") String nim);

    @Query("SELECT COUNT(m) FROM Mahasiswa m WHERE m.jurusan = :jurusan AND m.statusMahasiswa = :status")
    Long countByJurusanAndStatus(@Param("jurusan") String jurusan, @Param("status") StatusMahasiswa status);
}