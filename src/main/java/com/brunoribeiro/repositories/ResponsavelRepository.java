package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Responsavel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResponsavelRepository extends JpaRepository<Responsavel, UUID> {
    List<Responsavel> findAllByProfessorId(UUID professorId);
}
