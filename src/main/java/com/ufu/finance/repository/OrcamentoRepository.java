package com.ufu.finance.repository;

import com.ufu.finance.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findByUsuarioIdOrderByAnoDescMesDesc(Long usuarioId);

    List<Orcamento> findByUsuarioIdAndMesAndAno(Long usuarioId, Integer mes, Integer ano);

    Optional<Orcamento> findByUsuarioIdAndCategoriaIdAndMesAndAno(
            Long usuarioId, Integer categoriaId, Integer mes, Integer ano);

    boolean existsByUsuarioIdAndCategoriaIdAndMesAndAno(
            Long usuarioId, Integer categoriaId, Integer mes, Integer ano);
}
