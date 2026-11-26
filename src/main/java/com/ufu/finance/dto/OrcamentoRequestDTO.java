package com.ufu.finance.dto;

import com.ufu.finance.enums.Prioridade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrcamentoRequestDTO {

    @NotNull(message = "Categoria é obrigatória")
    private Integer categoriaId;

    @NotNull(message = "Mês é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    private Integer mes;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2000, message = "Ano deve ser entre 2000 e 2100")
    @Max(value = 2100, message = "Ano deve ser entre 2000 e 2100")
    private Integer ano;

    @NotNull(message = "Limite é obrigatório")
    @DecimalMin(value = "0.01", message = "Limite deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Limite deve ter no máximo 10 dígitos inteiros e 2 decimais")
    private BigDecimal limite;

    private Prioridade prioridade;
}
