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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.demo.ativo", havingValue = "true", matchIfMissing = true)
public class DadosDemonstracao implements ApplicationRunner {

    static final String NOME = "Conta Demonstração";
    static final String EMAIL = "demo@finplan.local";
    static final String SENHA = "demo1234";

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private static final TipoTransacao R = TipoTransacao.R;
    private static final TipoTransacao D = TipoTransacao.D;

    private record Lancamento(int mesesAtras, int dia, TipoTransacao tipo, String categoria,
                              String valor, String descricao) {
    }

    private record Limite(int mesesAtras, String categoria, String valor) {
    }

    private static final List<Lancamento> LANCAMENTOS = List.of(
            new Lancamento(2, 5, R, "Salário", "4200.00", "Salário"),
            new Lancamento(2, 18, R, "Freelance", "850.00", "Criação de site para cliente"),
            new Lancamento(2, 6, D, "Contas da casa", "1300.00", "Aluguel"),
            new Lancamento(2, 8, D, "Contas da casa", "176.35", "Conta de luz"),
            new Lancamento(2, 9, D, "Contas da casa", "99.90", "Internet"),
            new Lancamento(2, 7, D, "Compras", "512.80", "Supermercado do mês"),
            new Lancamento(2, 25, D, "Compras", "159.90", "Tênis de corrida"),
            new Lancamento(2, 12, D, "Transporte", "210.00", "Combustível"),
            new Lancamento(2, 15, D, "Lazer", "89.00", "Cinema e lanche"),
            new Lancamento(2, 22, D, "Saúde", "64.50", "Farmácia"),

            new Lancamento(1, 5, R, "Salário", "4200.00", "Salário"),
            new Lancamento(1, 6, D, "Contas da casa", "1300.00", "Aluguel"),
            new Lancamento(1, 8, D, "Contas da casa", "189.70", "Conta de luz"),
            new Lancamento(1, 9, D, "Contas da casa", "99.90", "Internet"),
            new Lancamento(1, 7, D, "Compras", "487.35", "Supermercado do mês"),
            new Lancamento(1, 11, D, "Transporte", "230.00", "Combustível"),
            new Lancamento(1, 14, D, "Transporte", "42.60", "Corrida de aplicativo"),
            new Lancamento(1, 16, D, "Saúde", "250.00", "Consulta médica"),
            new Lancamento(1, 20, D, "Lazer", "135.00", "Jantar de aniversário"),
            new Lancamento(1, 27, D, "Lazer", "180.00", "Ingresso de show"),

            new Lancamento(0, 5, R, "Salário", "4200.00", "Salário"),
            new Lancamento(0, 15, R, "Freelance", "600.00", "Manutenção de site"),
            new Lancamento(0, 6, D, "Contas da casa", "1300.00", "Aluguel"),
            new Lancamento(0, 8, D, "Contas da casa", "168.20", "Conta de luz"),
            new Lancamento(0, 9, D, "Contas da casa", "99.90", "Internet"),
            new Lancamento(0, 4, D, "Compras", "455.60", "Supermercado do mês"),
            new Lancamento(0, 18, D, "Compras", "64.90", "Utensílios de cozinha"),
            new Lancamento(0, 10, D, "Transporte", "215.00", "Combustível"),
            new Lancamento(0, 13, D, "Lazer", "120.00", "Restaurante com amigos"),
            new Lancamento(0, 19, D, "Lazer", "210.00", "Passeio de fim de semana"),
            new Lancamento(0, 21, D, "Saúde", "38.90", "Farmácia")
    );

    private static final List<Limite> LIMITES = List.of(
            new Limite(1, "Compras", "600.00"),
            new Limite(1, "Lazer", "300.00"),
            new Limite(1, "Transporte", "300.00"),
            new Limite(0, "Compras", "600.00"),
            new Limite(0, "Lazer", "300.00"),
            new Limite(0, "Transporte", "400.00"),
            new Limite(0, "Contas da casa", "2000.00"),
            new Limite(0, "Saúde", "150.00")
    );

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final Clock relogio;

    @Autowired
    public DadosDemonstracao(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
                             CategoriaRepository categoriaRepository, TransacaoRepository transacaoRepository,
                             OrcamentoRepository orcamentoRepository) {
        this(usuarioRepository, usuarioService, categoriaRepository, transacaoRepository,
                orcamentoRepository, Clock.systemDefaultZone());
    }

    DadosDemonstracao(UsuarioRepository usuarioRepository, UsuarioService usuarioService,
                      CategoriaRepository categoriaRepository, TransacaoRepository transacaoRepository,
                      OrcamentoRepository orcamentoRepository, Clock relogio) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.relogio = relogio;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmail(EMAIL)) {
            return;
        }

        Map<String, Categoria> categorias = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(Categoria::getNome, Function.identity()));
        boolean faltaCategoria = LANCAMENTOS.stream().anyMatch(l -> !categorias.containsKey(l.categoria()))
                || LIMITES.stream().anyMatch(l -> !categorias.containsKey(l.categoria()));
        if (faltaCategoria) {
            log.warn("Conta de demonstração não criada: as categorias padrão não estão no banco");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(NOME);
        usuario.setEmail(EMAIL);
        usuario.setSenha(usuarioService.getPasswordEncoder().encode(SENHA));
        usuario = usuarioRepository.save(usuario);

        LocalDateTime agora = LocalDateTime.now(relogio).withNano(0);
        LocalDate hoje = agora.toLocalDate();

        Usuario dono = usuario;
        List<Transacao> transacoes = LANCAMENTOS.stream()
                .filter(l -> l.mesesAtras() > 0 || l.dia() <= hoje.getDayOfMonth())
                .map(l -> transacao(dono, categorias.get(l.categoria()), l, hoje, agora))
                .toList();
        transacaoRepository.saveAll(transacoes);

        List<Orcamento> orcamentos = LIMITES.stream()
                .map(l -> orcamento(dono, categorias.get(l.categoria()), l, hoje))
                .toList();
        orcamentoRepository.saveAll(orcamentos);

        log.info("Conta de demonstração criada ({}) com {} transações e {} orçamentos",
                EMAIL, transacoes.size(), orcamentos.size());
    }

    private static Transacao transacao(Usuario usuario, Categoria categoria, Lancamento lancamento,
                                       LocalDate hoje, LocalDateTime agora) {
        LocalDate data = hoje.minusMonths(lancamento.mesesAtras()).withDayOfMonth(lancamento.dia());
        LocalDateTime dataHora = data.atTime(LocalTime.of(8 + lancamento.dia() % 12, 30));
        if (dataHora.isAfter(agora)) {
            dataHora = agora;
        }

        Transacao transacao = new Transacao();
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setTipo(lancamento.tipo());
        transacao.setValor(new BigDecimal(lancamento.valor()));
        transacao.setDescricao(lancamento.descricao());
        transacao.setDataHoraTransacao(dataHora);
        return transacao;
    }

    private static Orcamento orcamento(Usuario usuario, Categoria categoria, Limite limite, LocalDate hoje) {
        LocalDate mes = hoje.minusMonths(limite.mesesAtras());

        Orcamento orcamento = new Orcamento();
        orcamento.setUsuario(usuario);
        orcamento.setCategoria(categoria);
        orcamento.setMes(mes.getMonthValue());
        orcamento.setAno(mes.getYear());
        orcamento.setLimite(new BigDecimal(limite.valor()));
        return orcamento;
    }
}
