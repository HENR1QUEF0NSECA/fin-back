package com.ufu.finance.dto;

import java.math.BigDecimal;
import java.util.List;

/** Evolução mês a mês de receitas e despesas em um ano — para gráfico de linha */
public record EvolucaoMensalDTO(int ano, List<PontoMensalDTO> receitas, List<PontoMensalDTO> despesas) {

    public record PontoMensalDTO(int mes, BigDecimal total) {
    }
}
