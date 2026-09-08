package com.brunoribeiro.Entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "professores")
public class Professor {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "intervalo_minimo")
    private Integer intervaloMinimo;

    public Professor() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Integer getIntervaloMinimo() {
        return intervaloMinimo;
    }

    public void setIntervaloMinimo(Integer intervaloMinimo) {
        this.intervaloMinimo = intervaloMinimo;
    }
}
