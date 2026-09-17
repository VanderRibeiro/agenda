package com.brunoribeiro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brunoribeiro.entities.Professor;
import java.util.UUID;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
}
