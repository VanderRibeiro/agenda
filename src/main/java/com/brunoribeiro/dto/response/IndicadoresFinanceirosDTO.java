package com.brunoribeiro.dto.response;

import java.math.BigDecimal;

public record IndicadoresFinanceirosDTO(
        BigDecimal valorGanho,
        BigDecimal valorAReceber,
        BigDecimal valorCanceladas,
        BigDecimal valorAdiadas
) {
}
