package com.brunoribeiro.dto.request;

import com.brunoribeiro.entities.Professor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfessorRequestDTO(
        @NotBlank(message = "O nome do professor é obrigatório")
        @Size(max = 100, message = "O nome do professor deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email do professor é obrigatório")
        @Email(message = "Informe um email válido")
        @Size(max = 150, message = "O email do professor deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "A senha do professor é obrigatória")
        @Size(max = 255, message = "A senha do professor deve ter no máximo 255 caracteres")
        String senhaHash,

        @NotNull(message = "O intervalo mínimo é obrigatório")
        Integer intervaloMinimo
) {
    public Professor toEntity() {
        Professor professor = new Professor();
        professor.setNome(this.nome);
        professor.setEmail(this.email);
        professor.setSenhaHash(this.senhaHash);
        professor.setIntervaloMinimo(this.intervaloMinimo);
        return professor;
    }
}
