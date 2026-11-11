package com.ufu.finance.repository;

import com.ufu.finance.entity.Transacao;
import com.ufu.finance.enums.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // ─── Listagens ────────────────────────────────────────────────────────────

    /** Todas as transações de um usuário, mais recentes primeiro */
    List<Transacao> findByUsuarioIdOrderByDataHoraTransacaoDesc(Long usuarioId);

    /** Transações de um usuário filtradas por tipo (R ou D) */
    List<Transacao> findByUsuarioIdAndTipoOrderByDataHoraTransacaoDesc(Long usuarioId, TipoTransacao tipo);

    /** Transações de um usuário em um intervalo de data/hora (inclusivo nas duas pontas) */
    List<Transacao> findByUsuarioIdAndDataHoraTransacaoBetweenOrderByDataHoraTransacaoDesc(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    /** Transações de um usuário dentro de um mês/ano específico */
    @Query("""
            SELECT t FROM Transacao t
            WHERE t.usuario.id = :usuarioId
              AND MONTH(t.dataHoraTransacao) = :mes
              AND YEAR(t.dataHoraTransacao)  = :ano
            ORDER BY t.dataHoraTransacao DESC
            """)
    List<Transacao> findByUsuarioIdAndMesAno(
            @Param("usuarioId") Long usuarioId,
            @Param("mes") int mes,
            @Param("ano") int ano);

    /** Transações de um usuário por categoria */
    List<Transacao> findByUsuarioIdAndCategoriaIdOrderByDataHoraTransacaoDesc(
            Long usuarioId, Integer categoriaId);

    // ─── Aggregations para Dashboard ─────────────────────────────────────────

    /** Soma total de um tipo (Receita ou Despesa) no mês */
    @Query("""
            SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
            WHERE t.usuario.id = :usuarioId
              AND t.tipo = :tipo
              AND MONTH(t.dataHoraTransacao) = :mes
              AND YEAR(t.dataHoraTransacao)  = :ano
            """)
    BigDecimal sumByTipoAndMesAno(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("mes") int mes,
            @Param("ano") int ano);

    /**
     * Soma das transações de uma categoria específica num mês/ano por tipo.
     * Usado pelo OrcamentoService (cálculo de `consumido`) e pelo TransacaoService
     * (cálculo de `alertaOrcamento` ao criar transação).
     */
    @Query("""
            SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
            WHERE t.usuario.id = :usuarioId
              AND t.categoria.id = :idCategoria
              AND t.tipo = :tipo
              AND MONTH(t.dataHoraTransacao) = :mes
              AND YEAR(t.dataHoraTransacao)  = :ano
            """)
    BigDecimal sumByCategoriaTipoMesAno(
            @Param("usuarioId") Long usuarioId,
            @Param("idCategoria") Integer idCategoria,
            @Param("tipo") TipoTransacao tipo,
            @Param("mes") int mes,
            @Param("ano") int ano);

    /**
     * Totais agrupados por categoria para o mês — útil para gráfico de pizza.
     * Retorna Object[]: [nomeCategoria (String), total (BigDecimal)]
     */
    @Query("""
            SELECT c.nome, COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            JOIN t.categoria c
            WHERE t.usuario.id = :usuarioId
              AND t.tipo = :tipo
              AND MONTH(t.dataHoraTransacao) = :mes
              AND YEAR(t.dataHoraTransacao)  = :ano
            GROUP BY c.nome
            ORDER BY SUM(t.valor) DESC
            """)
    List<Object[]> sumPorCategoriaNoMes(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("mes") int mes,
            @Param("ano") int ano);

    /**
     * Totais agrupados por mês no ano — útil para gráfico de barras/linha.
     * Retorna Object[]: [mes (Integer), total (BigDecimal)]
     */
    @Query("""
            SELECT MONTH(t.dataHoraTransacao), COALESCE(SUM(t.valor), 0)
            FROM Transacao t
            WHERE t.usuario.id = :usuarioId
              AND t.tipo = :tipo
              AND YEAR(t.dataHoraTransacao)  = :ano
            GROUP BY MONTH(t.dataHoraTransacao)
            ORDER BY MONTH(t.dataHoraTransacao)
            """)
    List<Object[]> sumPorMesNoAno(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") TipoTransacao tipo,
            @Param("ano") int ano);
}
