package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    Optional<Professor> findByEmail(String email);

}
