package com.brunoribeiro.entities;

import com.brunoribeiro.entities.enums.StatusAula;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "aulas")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @NotNull(message = "O professor é obrigatório")
    @Column(name = "professor_id", nullable = false, columnDefinition = "uuid")
    private UUID professorId;

    @NotNull(message = "O aluno é obrigatório")
    @Column(name = "aluno_id", nullable = false, columnDefinition = "uuid")
    private UUID alunoId;

    @Column(name = "aula_original_id", columnDefinition = "uuid")
    private UUID aulaOriginalId;

    @NotNull(message = "A data da aula é obrigatória")
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @NotNull(message = "O horário de início é obrigatório")
    @Column(name = "horario_inicio", nullable = false)
    private LocalTime horarioInicio;

    @NotNull(message = "O horário de fim é obrigatório")
    @Column(name = "horario_fim", nullable = false)
    private LocalTime horarioFim;

    @NotNull(message = "A duração da aula é obrigatória")
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @NotNull(message = "O valor da aula é obrigatório")
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @NotNull(message = "O status da aula é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAula status;

    @NotNull(message = "O campo pago é obrigatório")
    @Column(name = "pago", nullable = false)
    private Boolean pago;

    @Column(name = "date_pagamento")
    private LocalDate datePagamento;

    public Aula() {
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProfessorId() {
        return professorId;
    }

    public void setProfessorId(UUID professorId) {
        this.professorId = professorId;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(UUID alunoId) {
        this.alunoId = alunoId;
    }

    public UUID getAulaOriginalId() {
        return aulaOriginalId;
    }

    public void setAulaOriginalId(UUID aulaOriginalId) {
        this.aulaOriginalId = aulaOriginalId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(LocalTime horarioFim) {
        this.horarioFim = horarioFim;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusAula getStatus() {
        return status;
    }

    public void setStatus(StatusAula status) {
        this.status = status;
    }

    public Boolean getPago() {
        return pago;
    }

    public void setPago(Boolean pago) {
        this.pago = pago;
    }

    public LocalDate getDatePagamento() {
        return datePagamento;
    }

    public void setDatePagamento(LocalDate datePagamento) {
        this.datePagamento = datePagamento;
    }
}
