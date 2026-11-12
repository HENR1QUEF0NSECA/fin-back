package com.ufu.finance.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.ufu.finance.entity.Transacao;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transação como sai na API. Segue o mesmo padrão de nomes do orçamento:
 * "id" para o próprio registro e "usuarioId"/"categoriaId" para as referências.
 */
@Data
@NoArgsConstructor
public class TransacaoResponseDTO {

    private Long id;
    private Long usuarioId;
    private Integer categoriaId;
    private String nomeCategoria;
    private BigDecimal valor;
    private LocalDateTime dataHoraTransacao;
    private String descricao;
    private String tipo;         // "R" ou "D"
    private String tipoDescricao; // "Receita" ou "Despesa"

    /**
     * Alerta de orçamento (RF10). Populado APENAS em POST /api/transacoes —
     * nas listagens o campo é null e omitido do JSON via @JsonInclude.
     * true → essa transação fez o gasto na categoria/mês atingir >= 80% do limite.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean alertaOrcamento;

    public TransacaoResponseDTO(Transacao t) {
        this.id            = t.getId();
        this.usuarioId     = t.getUsuario().getId();
        this.categoriaId   = t.getCategoria().getId();
        this.nomeCategoria = t.getCategoria().getNome();
        this.valor         = t.getValor();
        this.dataHoraTransacao = t.getDataHoraTransacao();
        this.descricao     = t.getDescricao();
        this.tipo          = t.getTipo().name();
        this.tipoDescricao = t.getTipo().getDescricao();
    }
}
