package com.brunoribeiro.dto.response;

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
}
