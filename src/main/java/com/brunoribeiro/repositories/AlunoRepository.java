package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    List<Aluno> findAllByProfessorId(UUID professorId);
}
