// StaffRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByNip(String nip);
    List<Staff> findByDepartemen(String departemen);
    List<Staff> findByJabatan(String jabatan);
    List<Staff> findByStatusKerja(String statusKerja);
    boolean existsByNip(String nip);
}