package com.brunoribeiro.dto.request;

import com.brunoribeiro.entities.enums.StatusAula;
import jakarta.validation.constraints.NotNull;

public record AulaStatusRequestDTO(
        @NotNull(message = "O status da aula é obrigatório")
        StatusAula status
) {
}
