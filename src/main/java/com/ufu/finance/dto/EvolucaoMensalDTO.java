package com.ufu.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record EvolucaoMensalDTO(int ano, List<PontoMensalDTO> receitas, List<PontoMensalDTO> despesas) {

    public record PontoMensalDTO(int mes, BigDecimal total) {
    }
}
