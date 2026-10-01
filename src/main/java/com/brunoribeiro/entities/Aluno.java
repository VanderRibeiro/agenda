package com.brunoribeiro.entities;

import com.brunoribeiro.entities.enums.DiaSemana;
import com.brunoribeiro.entities.enums.FormaPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "alunos")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "professor_id", nullable = false, columnDefinition = "uuid")
    private UUID professorId;

    @Column(name = "responsavel_id", columnDefinition = "uuid")
    private UUID responsavelId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "endereco")
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Column(name = "valor_mensal")
    private BigDecimal valorMensal;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana_padrao")
    private DiaSemana diaSemanaPadrao;

    @Column(name = "horario_padrao")
    private LocalTime horarioPadrao;

    @Column(name = "duracao_padrao_min")
    private Integer duracaoPadraoMin;

    @Column(name = "valor_padrao")
    private BigDecimal valorPadrao;

    public Aluno() {
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

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(UUID responsavelId) {
        this.responsavelId = responsavelId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValorMensal() {
        return valorMensal;
    }

    public void setValorMensal(BigDecimal valorMensal) {
        this.valorMensal = valorMensal;
    }

    public DiaSemana getDiaSemanaPadrao() {
        return diaSemanaPadrao;
    }

    public void setDiaSemanaPadrao(DiaSemana diaSemanaPadrao) {
        this.diaSemanaPadrao = diaSemanaPadrao;
    }

    public LocalTime getHorarioPadrao() {
        return horarioPadrao;
    }

    public void setHorarioPadrao(LocalTime horarioPadrao) {
        this.horarioPadrao = horarioPadrao;
    }

    public Integer getDuracaoPadraoMin() {
        return duracaoPadraoMin;
    }

    public void setDuracaoPadraoMin(Integer duracaoPadraoMin) {
        this.duracaoPadraoMin = duracaoPadraoMin;
    }

    public BigDecimal getValorPadrao() {
        return valorPadrao;
    }

    public void setValorPadrao(BigDecimal valorPadrao) {
        this.valorPadrao = valorPadrao;
    }

}

