// PersonRepository.java
package com.university.academic.repository;

import com.university.academic.model.entity.Person;
import com.university.academic.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByEmail(String email);
    List<Person> findByRole(Role role);
    boolean existsByEmail(String email);
    List<Person> findByNamaContainingIgnoreCase(String nama);
}