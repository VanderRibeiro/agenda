package com.brunoribeiro.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "responsaveis")
public class Responsavel {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "professor", columnDefinition = "uuid")
    private UUID professor;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "telefone", length = 30)
    private String telefone;

    public Responsavel() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProfessor() {
        return professor;
    }

    public void setProfessor(UUID professor) {
        this.professor = professor;
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
}
