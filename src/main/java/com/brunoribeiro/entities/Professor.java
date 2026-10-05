package com.brunoribeiro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "professores")
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @NotBlank(message = "O nome do professor é obrigatório")
    @Size(max = 100, message = "O nome do professor deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O email do professor é obrigatório")
    @Email(message = "Informe um email válido")
    @Size(max = 150, message = "O email do professor deve ter no máximo 150 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "A senha do professor é obrigatória")
    @Size(max = 255, message = "A senha do professor deve ter no máximo 255 caracteres")
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @NotNull(message = "O intervalo mínimo é obrigatório")
    @Column(name = "intervalo_minimo", nullable = false)
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
