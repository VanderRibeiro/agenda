package com.brunoribeiro.repositories;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.entities.enums.StatusAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    List<Aula> findAllByProfessorId(UUID professorId);

    @Query("select a from Aula a where a.professorId = :professorId " +
            "and a.data = :data " +
            "and a.horarioInicio < :fim " +
            "and a.horarioFim > :inicio " +
            "and a.status not in :ignoredStatuses")
    List<Aula> findOverlappingAulas(
            @Param("professorId") UUID professorId,
            @Param("data") LocalDate data,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim,
            @Param("ignoredStatuses") Collection<StatusAula> ignoredStatuses
    );
}
