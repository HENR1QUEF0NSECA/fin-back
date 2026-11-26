package com.ufu.finance.config;

import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Orcamento;
import com.ufu.finance.entity.Transacao;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.OrcamentoRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import com.ufu.finance.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DadosDemonstracaoTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Test
    void naoRecriaAContaQuandoElaJaExiste() {
        when(usuarioRepository.existsByEmail(DadosDemonstracao.EMAIL)).thenReturn(true);

        dados(LocalDateTime.of(2026, 11, 26, 10, 0)).run(null);

        verify(usuarioRepository, never()).save(any());
        verify(transacaoRepository, never()).saveAll(any());
        verify(orcamentoRepository, never()).saveAll(any());
    }

    @Test
    void semAsCategoriasPadraoNaoCriaNada() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria(1, "Transporte")));

        dados(LocalDateTime.of(2026, 11, 26, 10, 0)).run(null);

        verify(usuarioRepository, never()).save(any());
        verify(transacaoRepository, never()).saveAll(any());
    }

    @Test
    void criaAContaComTresMesesDeDadosCorretos() {
        LocalDateTime agora = LocalDateTime.of(2026, 11, 26, 10, 0);
        prepararCriacao();

        dados(agora).run(null);

        Usuario usuario = usuarioSalvo();
        assertThat(usuario.getNome()).isEqualTo("Conta Demonstração");
        assertThat(usuario.getEmail()).isEqualTo("demo@finplan.local");
        assertThat(new BCryptPasswordEncoder().matches("demo1234", usuario.getSenha())).isTrue();

        List<Transacao> transacoes = transacoesSalvas();
        assertThat(transacoes).hasSize(31);
        assertThat(transacoes).allSatisfy(t -> {
            assertThat(t.getUsuario()).isSameAs(usuario);
            assertThat(t.getCategoria()).isNotNull();
            assertThat(t.getValor()).isPositive();
            assertThat(t.getDescricao()).isNotBlank();
            assertThat(t.getDataHoraTransacao())
                    .isAfterOrEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0))
                    .isBeforeOrEqualTo(agora);
        });
        assertThat(transacoes).anyMatch(t -> t.getTipo() == TipoTransacao.R);
        assertThat(transacoes).anyMatch(t -> t.getTipo() == TipoTransacao.D);

        List<Orcamento> orcamentos = orcamentosSalvos();
        assertThat(orcamentos).hasSize(8);
        assertThat(orcamentos).allSatisfy(o -> {
            assertThat(o.getUsuario()).isSameAs(usuario);
            assertThat(o.getLimite()).isPositive();
            assertThat(o.getAno()).isEqualTo(2026);
            assertThat(o.getMes()).isIn(10, 11);
        });
    }

    @Test
    void noInicioDoMesNaoCriaLancamentosNoFuturo() {
        LocalDateTime agora = LocalDateTime.of(2027, 1, 5, 7, 0);
        prepararCriacao();

        dados(agora).run(null);

        List<Transacao> transacoes = transacoesSalvas();
        assertThat(transacoes).allSatisfy(t -> assertThat(t.getDataHoraTransacao()).isBeforeOrEqualTo(agora));
        assertThat(transacoes).filteredOn(t -> t.getDataHoraTransacao().toLocalDate().getMonthValue() == 1)
                .extracting(Transacao::getDescricao)
                .containsExactlyInAnyOrder("Salário", "Supermercado do mês");
        assertThat(transacoes).anyMatch(t -> t.getDataHoraTransacao().toLocalDate()
                .equals(LocalDate.of(2026, 11, 5)));
    }

    private void prepararCriacao() {
        AtomicInteger id = new AtomicInteger();
        List<Categoria> categorias = List.of("Transporte", "Lazer", "Contas da casa", "Saúde", "Compras",
                "Salário", "Freelance").stream().map(nome -> categoria(id.incrementAndGet(), nome)).toList();
        when(categoriaRepository.findAll()).thenReturn(categorias);
        when(usuarioService.getPasswordEncoder()).thenReturn(new BCryptPasswordEncoder(4));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }

    private DadosDemonstracao dados(LocalDateTime agora) {
        Clock relogio = Clock.fixed(agora.atZone(FUSO).toInstant(), FUSO);
        return new DadosDemonstracao(usuarioRepository, usuarioService, categoriaRepository,
                transacaoRepository, orcamentoRepository, relogio);
    }

    private Usuario usuarioSalvo() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<Transacao> transacoesSalvas() {
        ArgumentCaptor<List<Transacao>> captor = ArgumentCaptor.forClass(List.class);
        verify(transacaoRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<Orcamento> orcamentosSalvos() {
        ArgumentCaptor<List<Orcamento>> captor = ArgumentCaptor.forClass(List.class);
        verify(orcamentoRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    private static Categoria categoria(int id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        return categoria;
    }
}
