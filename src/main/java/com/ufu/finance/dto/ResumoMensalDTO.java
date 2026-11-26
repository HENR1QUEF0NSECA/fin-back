package com.ufu.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumoMensalDTO(
        int mes,
        int ano,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        BigDecimal totalInvestido,
        List<GraficoCategoriaDTO> despesasPorCategoria,
        List<GraficoCategoriaDTO> receitasPorCategoria) {

    public ResumoMensalDTO(int mes, int ano,
                           BigDecimal totalReceitas,
                           BigDecimal totalDespesas,
                           BigDecimal totalInvestido,
                           List<GraficoCategoriaDTO> despesasPorCategoria,
                           List<GraficoCategoriaDTO> receitasPorCategoria) {
        this(mes, ano, totalReceitas, totalDespesas, totalReceitas.subtract(totalDespesas),
                totalInvestido, despesasPorCategoria, receitasPorCategoria);
    }
}
