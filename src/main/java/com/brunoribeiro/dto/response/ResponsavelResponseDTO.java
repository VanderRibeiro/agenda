package com.brunoribeiro.dto.response;

import java.util.UUID;

public record ResponsavelResponseDTO(
        UUID id,
        UUID professorId,
        String nome,
        String telefone
) {
}
