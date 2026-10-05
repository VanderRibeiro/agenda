package com.brunoribeiro.dto.response;

import com.brunoribeiro.entities.Responsavel;

import java.util.UUID;

public record ResponsavelResponseDTO(
        UUID id,
        UUID professorId,
        String nome,
        String telefone
) {
    public static ResponsavelResponseDTO fromEntity(Responsavel responsavel) {
        if (responsavel == null) {
            return null;
        }

        return new ResponsavelResponseDTO(
                responsavel.getId(),
                responsavel.getProfessorId(),
                responsavel.getNome(),
                responsavel.getTelefone()
        );
    }
}
