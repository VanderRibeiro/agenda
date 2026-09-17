package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AlunoRepository extends JpaRepository<Aluno, java.util.UUID> {
}
