package com.ufu.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para requisição de criação/atualização de orçamento
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrcamentoRequestDTO {

    @NotNull(message = "Categoria é obrigatória")
    private Integer idCategoria;

    @NotNull(message = "Mês é obrigatório")
    private Integer mes;

    @NotNull(message = "Ano é obrigatório")
    private Integer ano;

    @NotNull(message = "Limite é obrigatório")
    @DecimalMin(value = "0.01", message = "Limite deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Limite deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal limite;
}
