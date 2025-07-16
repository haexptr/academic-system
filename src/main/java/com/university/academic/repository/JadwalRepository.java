// JadwalRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Jadwal;
import com.university.academic.model.entity.Dosen;
import com.university.academic.model.entity.MataKuliah;
import com.university.academic.model.enums.Hari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface JadwalRepository extends JpaRepository<Jadwal, Long> {
    List<Jadwal> findByDosen(Dosen dosen);
    List<Jadwal> findByMataKuliah(MataKuliah mataKuliah);
    List<Jadwal> findByHari(Hari hari);
    List<Jadwal> findByRuangan(String ruangan);
    List<Jadwal> findByTahunAkademik(String tahunAkademik);

    // TAMBAHAN BARU - Query dengan JOIN FETCH untuk mengatasi LazyInitializationException
    @Query("SELECT j FROM Jadwal j " +
            "LEFT JOIN FETCH j.mataKuliah " +
            "LEFT JOIN FETCH j.dosen")
    List<Jadwal> findAllWithMataKuliahAndDosen();

    @Query("SELECT j FROM Jadwal j " +
            "LEFT JOIN FETCH j.mataKuliah " +
            "LEFT JOIN FETCH j.dosen " +
            "WHERE j.dosen = :dosen")
    List<Jadwal> findByDosenWithMataKuliah(@Param("dosen") Dosen dosen);

    @Query("SELECT j FROM Jadwal j " +
            "LEFT JOIN FETCH j.mataKuliah " +
            "LEFT JOIN FETCH j.dosen " +
            "WHERE j.hari = :hari")
    List<Jadwal> findByHariWithMataKuliahAndDosen(@Param("hari") Hari hari);

    @Query("SELECT j FROM Jadwal j WHERE j.hari = :hari AND j.ruangan = :ruangan AND " +
            "((j.jamMulai <= :jamMulai AND j.jamSelesai > :jamMulai) OR " +
            "(j.jamMulai < :jamSelesai AND j.jamSelesai >= :jamSelesai) OR " +
            "(j.jamMulai >= :jamMulai AND j.jamSelesai <= :jamSelesai))")
    List<Jadwal> findConflictingSchedules(@Param("hari") Hari hari,
                                          @Param("ruangan") String ruangan,
                                          @Param("jamMulai") LocalTime jamMulai,
                                          @Param("jamSelesai") LocalTime jamSelesai);

    @Query("SELECT j FROM Jadwal j WHERE j.dosen = :dosen AND j.hari = :hari AND " +
            "((j.jamMulai <= :jamMulai AND j.jamSelesai > :jamMulai) OR " +
            "(j.jamMulai < :jamSelesai AND j.jamSelesai >= :jamSelesai) OR " +
            "(j.jamMulai >= :jamMulai AND j.jamSelesai <= :jamSelesai))")
    List<Jadwal> findDosenConflictingSchedules(@Param("dosen") Dosen dosen,
                                               @Param("hari") Hari hari,
                                               @Param("jamMulai") LocalTime jamMulai,
                                               @Param("jamSelesai") LocalTime jamSelesai);
}