package com.ufu.finance.dto;

import com.ufu.finance.entity.Orcamento;
import com.ufu.finance.enums.Prioridade;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrcamentoResponseDTO {

    private Long id;
    private Long usuarioId;
    private Integer categoriaId;
    private String nomeCategoria;
    private Integer mes;
    private Integer ano;
    private BigDecimal limite;
    private Prioridade prioridade;

    private BigDecimal consumido;

    private Double percentualUtilizado;

    private BigDecimal saldo;

    private Boolean alertaAtivo;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public OrcamentoResponseDTO(Orcamento orcamento) {
        this.id = orcamento.getId();
        this.usuarioId = orcamento.getUsuario().getId();
        this.categoriaId = orcamento.getCategoria().getId();
        this.nomeCategoria = orcamento.getCategoria().getNome();
        this.mes = orcamento.getMes();
        this.ano = orcamento.getAno();
        this.limite = orcamento.getLimite();
        this.prioridade = orcamento.getPrioridade();
        this.consumido = BigDecimal.ZERO;
        this.percentualUtilizado = 0.0;
        this.saldo = orcamento.getLimite();
        this.alertaAtivo = false;
        this.dataCriacao = orcamento.getDataCriacao();
        this.dataAtualizacao = orcamento.getDataAtualizacao();
    }

    public void atualizarConsumo(BigDecimal consumido) {
        this.consumido = consumido;
        this.saldo = this.limite.subtract(consumido).max(BigDecimal.ZERO);

        if (this.limite.compareTo(BigDecimal.ZERO) > 0) {
            this.percentualUtilizado = consumido
                    .divide(this.limite, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        this.alertaAtivo = this.percentualUtilizado >= 80.0;
    }
}

