package com.ufu.finance.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumo financeiro de um mês: totais de receita, despesa e saldo.
 * Inclui breakdown por categoria para montar gráficos.
 */
public record ResumoMensalDTO(
        int mes,
        int ano,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo, // receitas - despesas
        List<GraficoCategoriaDTO> despesasPorCategoria,
        List<GraficoCategoriaDTO> receitasPorCategoria) {

    /** Calcula o saldo a partir dos totais. */
    public ResumoMensalDTO(int mes, int ano,
                           BigDecimal totalReceitas,
                           BigDecimal totalDespesas,
                           List<GraficoCategoriaDTO> despesasPorCategoria,
                           List<GraficoCategoriaDTO> receitasPorCategoria) {
        this(mes, ano, totalReceitas, totalDespesas, totalReceitas.subtract(totalDespesas),
                despesasPorCategoria, receitasPorCategoria);
    }
}
