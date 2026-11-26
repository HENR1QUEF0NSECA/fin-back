package com.ufu.finance.dto;

import com.ufu.finance.entity.Investimento;
import com.ufu.finance.enums.TipoInvestimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestimentoResponseDTO(
        Long id,
        String descricao,
        TipoInvestimento tipo,
        BigDecimal valor,
        LocalDate data) {

    public InvestimentoResponseDTO(Investimento investimento) {
        this(investimento.getId(),
                investimento.getDescricao(),
                investimento.getTipo(),
                investimento.getValor(),
                investimento.getDataInvestimento());
    }
}
