package com.brunoribeiro.dto.response;

import com.brunoribeiro.entities.Professor;

import java.util.UUID;

public record ProfessorResponseDTO(
        UUID id,
        String nome,
        String email,
        Integer intervaloMinimo
) {
    public static ProfessorResponseDTO fromEntity(Professor professor) {
        if (professor == null) {
            return null;
        }

        return new ProfessorResponseDTO(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getIntervaloMinimo()
        );
    }
}
