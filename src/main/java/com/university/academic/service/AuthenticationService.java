// AuthenticationService.java
package com.university.academic.service;

import com.university.academic.model.entity.Person;
import com.university.academic.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Person> personOpt = personRepository.findByEmail(email);

        if (personOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        Person person = personOpt.get();

        return User.builder()
                .username(person.getEmail())
                .password(person.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + person.getRole().name())))
                .build();
    }

    public Optional<Person> getAuthenticatedPerson(String email) {
        return personRepository.findByEmail(email);
    }
}