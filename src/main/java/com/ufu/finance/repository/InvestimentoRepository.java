package com.ufu.finance.repository;

import com.ufu.finance.entity.Investimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {

    List<Investimento> findByUsuarioIdOrderByDataInvestimentoDescIdDesc(Long usuarioId);

    @Query("""
            SELECT COALESCE(SUM(i.valor), 0) FROM Investimento i
            WHERE i.usuario.id = :usuarioId
              AND MONTH(i.dataInvestimento) = :mes
              AND YEAR(i.dataInvestimento)  = :ano
            """)
    BigDecimal sumNoMes(
            @Param("usuarioId") Long usuarioId,
            @Param("mes") int mes,
            @Param("ano") int ano);

    @Query("""
            SELECT MONTH(i.dataInvestimento), COALESCE(SUM(i.valor), 0)
            FROM Investimento i
            WHERE i.usuario.id = :usuarioId
              AND YEAR(i.dataInvestimento) = :ano
            GROUP BY MONTH(i.dataInvestimento)
            ORDER BY MONTH(i.dataInvestimento)
            """)
    List<Object[]> sumPorMesNoAno(
            @Param("usuarioId") Long usuarioId,
            @Param("ano") int ano);
}
