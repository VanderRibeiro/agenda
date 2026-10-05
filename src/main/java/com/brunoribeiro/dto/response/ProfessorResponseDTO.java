package com.brunoribeiro.dto.response;

import java.util.UUID;

public record ProfessorResponseDTO(
        UUID id,
        String nome,
        String email,
        Integer intervaloMinimo
) {
}
