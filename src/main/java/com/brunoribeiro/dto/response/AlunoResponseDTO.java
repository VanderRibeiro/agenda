package com.brunoribeiro.dto.response;

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
}
