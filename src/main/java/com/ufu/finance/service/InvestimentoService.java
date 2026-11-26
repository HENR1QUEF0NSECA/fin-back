package com.ufu.finance.service;

import com.ufu.finance.dto.InvestimentoRequestDTO;
import com.ufu.finance.dto.InvestimentoResponseDTO;
import com.ufu.finance.entity.Investimento;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.exception.RecursoNaoEncontradoException;
import com.ufu.finance.exception.RequisicaoInvalidaException;
import com.ufu.finance.repository.InvestimentoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestimentoService {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);

    private final InvestimentoRepository investimentoRepository;

    private final UsuarioRepository usuarioRepository;

    public InvestimentoResponseDTO criar(InvestimentoRequestDTO dto, Long usuarioId) {
        if (dto.getData().isBefore(DATA_MINIMA)) {
            throw new RequisicaoInvalidaException("Data deve ser a partir de 01/01/2000");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));

        Investimento investimento = new Investimento();
        investimento.setUsuario(usuario);
        investimento.setDescricao(dto.getDescricao().trim());
        investimento.setTipo(dto.getTipo());
        investimento.setValor(dto.getValor());
        investimento.setDataInvestimento(dto.getData());

        return new InvestimentoResponseDTO(investimentoRepository.save(investimento));
    }

    public List<InvestimentoResponseDTO> listar(Long usuarioId) {
        return investimentoRepository.findByUsuarioIdOrderByDataInvestimentoDescIdDesc(usuarioId)
                .stream()
                .map(InvestimentoResponseDTO::new)
                .toList();
    }

    public void deletar(Long id, Long usuarioId) {
        Investimento investimento = investimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Investimento não encontrado"));

        if (!investimento.getUsuario().getId().equals(usuarioId)) {
            throw new AcessoNegadoException(
                    "Acesso negado: este investimento pertence a outro usuário");
        }

        investimentoRepository.delete(investimento);
    }
}
