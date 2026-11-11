package com.ufu.finance.entity;

import com.ufu.finance.enums.TipoTransacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    private Long id;

    /**
     * Usuário dono da transação — FK para a tabela usuarios.
     * Carregamento LAZY: o User só é buscado no banco quando explicitamente acessado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Categoria da transação — FK para a tabela categorias.
     * EAGER aqui pois sempre precisamos do nome da categoria na resposta.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "dh_transacao", nullable = false)
    private LocalDateTime dataHoraTransacao;

    @Column(length = 255)
    private String descricao;

    /**
     * Tipo salvo como texto de 1 caractere no banco: 'R' = Receita, 'D' = Despesa
     * (EnumType.STRING grava o nome da constante do enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private TipoTransacao tipo;

}