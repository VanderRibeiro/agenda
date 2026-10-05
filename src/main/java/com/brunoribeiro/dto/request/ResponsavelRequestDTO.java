package com.brunoribeiro.dto.request;

import com.brunoribeiro.entities.Responsavel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResponsavelRequestDTO(
        @NotBlank(message = "O nome do responsável é obrigatório")
        @Size(max = 100, message = "O nome do responsável deve ter no máximo 100 caracteres")
        String nome,

        @NotNull(message = "O professor é obrigatório")
        UUID professorId,

        @NotBlank(message = "O telefone do responsável é obrigatório")
        @Size(max = 30, message = "O telefone do responsável deve ter no máximo 30 caracteres")
        String telefone
) {
    public Responsavel toEntity() {
        Responsavel responsavel = new Responsavel();
        responsavel.setNome(this.nome);
        responsavel.setProfessorId(this.professorId);
        responsavel.setTelefone(this.telefone);
        return responsavel;
    }
}
