package com.brunoribeiro.dto.response;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.entities.enums.StatusAula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AulaResponseDTO(
        UUID id,
        UUID professorId,
        UUID alunoId,
        UUID aulaOriginalId,
        LocalDate data,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        Integer duracaoMinutos,
        BigDecimal valor,
        StatusAula status,
        Boolean pago,
        LocalDate datePagamento
) {
    public static AulaResponseDTO fromEntity(Aula aula) {
        if (aula == null) {
            return null;
        }

        return new AulaResponseDTO(
                aula.getId(),
                aula.getProfessorId(),
                aula.getAlunoId(),
                aula.getAulaOriginalId(),
                aula.getData(),
                aula.getHorarioInicio(),
                aula.getHorarioFim(),
                aula.getDuracaoMinutos(),
                aula.getValor(),
                aula.getStatus(),
                aula.getPago(),
                aula.getDatePagamento()
        );
    }
}
