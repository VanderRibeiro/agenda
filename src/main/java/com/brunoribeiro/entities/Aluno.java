package com.brunoribeiro.entities;

import com.brunoribeiro.entities.enums.DiaSemana;
import com.brunoribeiro.entities.enums.FormaPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    @NotNull(message = "O professor é obrigatório")
    @Column(name = "professor_id", nullable = false, columnDefinition = "uuid")
    private UUID professorId;

    @Column(name = "responsavel_id", columnDefinition = "uuid")
    private UUID responsavelId;

    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(max = 100, message = "O nome do aluno deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Email(message = "Informe um email válido")
    @Size(max = 150, message = "O email do aluno deve ter no máximo 150 caracteres")
    @Column(name = "email", length = 150)
    private String email;

    @NotBlank(message = "O telefone do aluno é obrigatório")
    @Size(max = 30, message = "O telefone do aluno deve ter no máximo 30 caracteres")
    @Column(name = "telefone", nullable = false, length = 30)
    private String telefone;

    @Size(max = 255, message = "O endereço do aluno deve ter no máximo 255 caracteres")
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

