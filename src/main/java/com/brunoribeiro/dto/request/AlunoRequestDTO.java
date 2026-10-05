package com.brunoribeiro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AlunoRequestDTO(
        @NotNull(message = "O professor é obrigatório")
        UUID professorId,

        UUID responsavelId,

        @NotBlank(message = "O nome do aluno é obrigatório")
        @Size(max = 100, message = "O nome do aluno deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O email do aluno é obrigatório")
        @Email(message = "Informe um email válido")
        @Size(max = 150, message = "O email do aluno deve ter no máximo 150 caracteres")
        String email,

        @NotBlank(message = "O telefone do aluno é obrigatório")
        @Size(max = 30, message = "O telefone do aluno deve ter no máximo 30 caracteres")
        String telefone,

        @Size(max = 255, message = "O endereço do aluno deve ter no máximo 255 caracteres")
        String endereco
) {
}
