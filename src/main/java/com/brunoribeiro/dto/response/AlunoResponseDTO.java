package com.brunoribeiro.dto.response;

import com.brunoribeiro.entities.Aluno;

import java.util.UUID;

public record AlunoResponseDTO(
        UUID id,
        UUID professorId,
        UUID responsavelId,
        String nome,
        String email,
        String telefone,
        String endereco
) {
    public static AlunoResponseDTO fromEntity(Aluno aluno) {
        if (aluno == null) {
            return null;
        }

        return new AlunoResponseDTO(
                aluno.getId(),
                aluno.getProfessorId(),
                aluno.getResponsavelId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getTelefone(),
                aluno.getEndereco()
        );
    }
}
