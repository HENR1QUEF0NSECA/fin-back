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

    List<Transacao> findByUsuarioIdOrderByDataHoraTransacaoDesc(Long usuarioId);

    List<Transacao> findByUsuarioIdAndTipoOrderByDataHoraTransacaoDesc(Long usuarioId, TipoTransacao tipo);

    List<Transacao> findByUsuarioIdAndDataHoraTransacaoBetweenOrderByDataHoraTransacaoDesc(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

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

    List<Transacao> findByUsuarioIdAndCategoriaIdOrderByDataHoraTransacaoDesc(
            Long usuarioId, Integer categoriaId);

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

    @Query("""
            SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
            WHERE t.usuario.id = :usuarioId
              AND t.categoria.id = :categoriaId
              AND t.tipo = :tipo
              AND MONTH(t.dataHoraTransacao) = :mes
              AND YEAR(t.dataHoraTransacao)  = :ano
            """)
    BigDecimal sumByCategoriaTipoMesAno(
            @Param("usuarioId") Long usuarioId,
            @Param("categoriaId") Integer categoriaId,
            @Param("tipo") TipoTransacao tipo,
            @Param("mes") int mes,
            @Param("ano") int ano);

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
