// PersonService.java
package com.university.academic.service;

import com.university.academic.model.entity.Person;
import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.entity.Dosen;
import com.university.academic.model.entity.Staff;
import com.university.academic.model.enums.Role;
import com.university.academic.repository.PersonRepository;
import com.university.academic.repository.MahasiswaRepository;
import com.university.academic.repository.DosenRepository;
import com.university.academic.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final MahasiswaRepository mahasiswaRepository;
    private final DosenRepository dosenRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public Optional<Person> findByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    public List<Person> findByRole(Role role) {
        return personRepository.findByRole(role);
    }

    public Person save(Person person) {
        // Encode password jika belum di-encode
        if (!person.getPassword().startsWith("$2a$")) {
            person.setPassword(passwordEncoder.encode(person.getPassword()));
        }
        return personRepository.save(person);
    }

    public Mahasiswa saveMahasiswa(Mahasiswa mahasiswa) {
        mahasiswa.setRole(Role.MAHASISWA);
        if (!mahasiswa.getPassword().startsWith("$2a$")) {
            mahasiswa.setPassword(passwordEncoder.encode(mahasiswa.getPassword()));
        }
        return mahasiswaRepository.save(mahasiswa);
    }

    public Dosen saveDosen(Dosen dosen) {
        dosen.setRole(Role.DOSEN);
        if (!dosen.getPassword().startsWith("$2a$")) {
            dosen.setPassword(passwordEncoder.encode(dosen.getPassword()));
        }
        return dosenRepository.save(dosen);
    }

    public Staff saveStaff(Staff staff) {
        staff.setRole(Role.STAFF);
        if (!staff.getPassword().startsWith("$2a$")) {
            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        }
        return staffRepository.save(staff);
    }

    public void deleteById(Long id) {
        personRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    public List<Person> searchByName(String name) {
        return personRepository.findByNamaContainingIgnoreCase(name);
    }

    // Mahasiswa specific methods
    public List<Mahasiswa> findAllMahasiswa() {
        return mahasiswaRepository.findAll();
    }

    public Optional<Mahasiswa> findMahasiswaByNim(String nim) {
        return mahasiswaRepository.findByNim(nim);
    }

    public List<Mahasiswa> findMahasiswaByJurusan(String jurusan) {
        return mahasiswaRepository.findByJurusan(jurusan);
    }

    // Dosen specific methods
    public List<Dosen> findAllDosen() {
        return dosenRepository.findAll();
    }

    public Optional<Dosen> findDosenByNip(String nip) {
        return dosenRepository.findByNip(nip);
    }

    public List<Dosen> findDosenByFakultas(String fakultas) {
        return dosenRepository.findByFakultas(fakultas);
    }

    // Staff specific methods
    public List<Staff> findAllStaff() {
        return staffRepository.findAll();
    }

    public Optional<Staff> findStaffByNip(String nip) {
        return staffRepository.findByNip(nip);
    }
}