package com.brunoribeiro.dto.request;

import com.brunoribeiro.entities.Aula;
import com.brunoribeiro.entities.enums.StatusAula;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AulaRequestDTO(
        @NotNull(message = "O professor é obrigatório")
        UUID professorId,

        @NotNull(message = "O aluno é obrigatório")
        UUID alunoId,

        UUID aulaOriginalId,

        @NotNull(message = "A data da aula é obrigatória")
        LocalDate data,

        @NotNull(message = "O horário de início é obrigatório")
        LocalTime horarioInicio,

        @NotNull(message = "O horário de fim é obrigatório")
        LocalTime horarioFim,

        @NotNull(message = "A duração da aula é obrigatória")
        Integer duracaoMinutos,

        @NotNull(message = "O valor da aula é obrigatório")
        BigDecimal valor,

        @NotNull(message = "O status da aula é obrigatório")
        StatusAula status,

        @NotNull(message = "O campo pago é obrigatório")
        Boolean pago,

        LocalDate datePagamento
) {
    public Aula toEntity() {
        Aula aula = new Aula();
        aula.setProfessorId(this.professorId);
        aula.setAlunoId(this.alunoId);
        aula.setAulaOriginalId(this.aulaOriginalId);
        aula.setData(this.data);
        aula.setHorarioInicio(this.horarioInicio);
        aula.setHorarioFim(this.horarioFim);
        aula.setDuracaoMinutos(this.duracaoMinutos);
        aula.setValor(this.valor);
        aula.setStatus(this.status);
        aula.setPago(this.pago);
        aula.setDatePagamento(this.datePagamento);
        return aula;
    }
}
