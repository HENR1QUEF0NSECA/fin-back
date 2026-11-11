package com.ufu.finance.dto;

import java.math.BigDecimal;

/** Um slice do gráfico de pizza: categoria + valor total */
public record GraficoCategoriaDTO(String categoria, BigDecimal total) {
}
