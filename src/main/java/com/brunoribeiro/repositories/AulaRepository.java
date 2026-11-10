package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.entities.enums.StatusAula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    List<Aula> findAllByProfessorId(UUID professorId);

    List<Aula> findAllByProfessorIdAndDataAndStatusNotIn(
            UUID professorId,
            LocalDate data,
            Collection<StatusAula> status
    );
}
