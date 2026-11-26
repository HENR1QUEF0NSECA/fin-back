package com.ufu.finance;

import com.jayway.jsonpath.JsonPath;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracaoTest {

    private static final String SENHA = "senha123";

    private static final String JSON_UTF8 = "application/json;charset=UTF-8";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Integer categoriaId;

    @BeforeEach
    void criarCategoria() {
        categoriaId = categoriaRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNome("Transporte");
                    return categoriaRepository.save(categoria);
                })
                .getId();
    }

    @Test
    @DisplayName("cadastro, login e /usuarios/me devolvem os dados do usuário, sem a senha")
    void cadastroLoginEPerfil() throws Exception {
        String email = emailNovo();
        registrar(email.toUpperCase())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist());

        String token = login(email);

        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nome").value("Usuário Teste"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("e-mail repetido, mesmo com maiúsculas, devolve 409")
    void emailDuplicado() throws Exception {
        String email = emailNovo();
        registrar(email).andExpect(status().isCreated());

        registrar(email.toUpperCase())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Email já está em uso"));
    }

    @Test
    @DisplayName("login com senha errada devolve 401")
    void loginComSenhaErrada() throws Exception {
        String email = emailNovo();
        registrar(email);

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'email':'%s','senha':'errada1'}", email)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Email ou senha inválidos"));
    }

    @Test
    @DisplayName("rota protegida sem token devolve 401 no formato padrão de erro")
    void rotaProtegidaSemToken() throws Exception {
        mvc.perform(get("/api/transacoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Token não fornecido"))
                .andExpect(jsonPath("$.timestamp").exists());

        mvc.perform(get("/api/transacoes").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Token inválido ou expirado"));
    }

    @Test
    @DisplayName("não existe mais rota para listar todos os usuários")
    void semListagemDeUsuarios() throws Exception {
        String token = novoUsuarioLogado();
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("transação criada e listada usa id, usuarioId e categoriaId")
    void criaEListaTransacao() throws Exception {
        String token = novoUsuarioLogado();

        comToken(post("/api/transacoes"), token,
                "{'tipo':'D','valor':50,'categoriaId':%d,'descricao':'Ônibus'}", categoriaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.usuarioId").isNumber())
                .andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.descricao").value("Ônibus"))
                .andExpect(jsonPath("$.alertaOrcamento").value(false));

        mvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoriaId").value(categoriaId))
                .andExpect(jsonPath("$[0].alertaOrcamento").doesNotExist());
    }

    @Test
    @DisplayName("transação é alterada pelo dono, inclusive a data, e não por outro usuário")
    void alteraTransacao() throws Exception {
        String token = novoUsuarioLogado();
        Integer outraCategoriaId = outraCategoria();

        String resposta = comToken(post("/api/transacoes"), token,
                "{'tipo':'D','valor':50,'categoriaId':%d,'descricao':'Ônibus','data':'2025-01-15'}", categoriaId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataHoraTransacao").value(startsWith("2025-01-15T")))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        comToken(put("/api/transacoes/" + id), token,
                "{'tipo':'R','valor':80.5,'categoriaId':%d,'descricao':'  Reembolso  ','data':'2025-03-10'}",
                outraCategoriaId)
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.tipo").value("R"))
                .andExpect(jsonPath("$.valor").value(80.5))
                .andExpect(jsonPath("$.categoriaId").value(outraCategoriaId))
                .andExpect(jsonPath("$.descricao").value("Reembolso"))
                .andExpect(jsonPath("$.dataHoraTransacao").value(startsWith("2025-03-10T")))
                .andExpect(jsonPath("$.alertaOrcamento").value(false));

        mvc.perform(get("/api/transacoes/mes?mes=3&ano=2025").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].descricao").value("Reembolso"));

        String outro = novoUsuarioLogado();
        comToken(put("/api/transacoes/" + id), outro, "{'tipo':'D','valor':1,'categoriaId':%d}", categoriaId)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado: você não pode alterar esta transação"));

        comToken(put("/api/transacoes/999999"), token, "{'tipo':'D','valor':1,'categoriaId':%d}", categoriaId)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Transação não encontrada"));
    }

    @Test
    @DisplayName("data futura, anterior a 2000 ou inexistente devolve 400")
    void dataDaTransacaoInvalida() throws Exception {
        String token = novoUsuarioLogado();
        String corpo = "{'tipo':'D','valor':10,'categoriaId':%d,'data':'%s'}";

        comToken(post("/api/transacoes"), token, corpo, categoriaId, LocalDate.now().plusDays(1).toString())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.data").value("Data não pode ser no futuro"));

        comToken(post("/api/transacoes"), token, corpo, categoriaId, "1999-12-31")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Data deve ser a partir de 01/01/2000"));

        comToken(post("/api/transacoes"), token, corpo, categoriaId, "2025-02-30")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.data").value("Valor inválido. Use o formato AAAA-MM-DD"));
    }

    @Test
    @DisplayName("orçamento é alterado pelo dono e devolve o novo limite")
    void alteraOrcamento() throws Exception {
        String token = novoUsuarioLogado();
        String resposta = comToken(post("/api/orcamentos"), token,
                "{'categoriaId':%d,'mes':5,'ano':2030,'limite':100}", categoriaId)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        comToken(put("/api/orcamentos/" + id), token,
                "{'categoriaId':%d,'mes':6,'ano':2030,'limite':250.75}", categoriaId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.mes").value(6))
                .andExpect(jsonPath("$.limite").value(250.75));

        mvc.perform(get("/api/orcamentos/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value(6))
                .andExpect(jsonPath("$.limite").value(250.75));
    }

    @Test
    @DisplayName("despesa que leva o gasto a 80% do orçamento liga o alerta")
    void alertaDeOrcamento() throws Exception {
        String token = novoUsuarioLogado();
        LocalDate hoje = LocalDate.now();
        comToken(post("/api/orcamentos"), token,
                "{'categoriaId':%d,'mes':%d,'ano':%d,'limite':100}",
                categoriaId, hoje.getMonthValue(), hoje.getYear())
                .andExpect(status().isCreated());

        comToken(post("/api/transacoes"), token, "{'tipo':'D','valor':79,'categoriaId':%d}", categoriaId)
                .andExpect(jsonPath("$.alertaOrcamento").value(false));
        comToken(post("/api/transacoes"), token, "{'tipo':'D','valor':1,'categoriaId':%d}", categoriaId)
                .andExpect(jsonPath("$.alertaOrcamento").value(true));
    }

    @Test
    @DisplayName("orçamento repetido devolve 409 e de outro usuário devolve 403")
    void orcamentoDuplicadoEDeOutroUsuario() throws Exception {
        String dono = novoUsuarioLogado();
        String corpo = "{'categoriaId':%d,'mes':1,'ano':2030,'limite':100}";

        String resposta = comToken(post("/api/orcamentos"), dono, corpo, categoriaId)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        comToken(post("/api/orcamentos"), dono, corpo, categoriaId)
                .andExpect(status().isConflict());

        String outro = novoUsuarioLogado();
        mvc.perform(delete("/api/orcamentos/" + id).header("Authorization", "Bearer " + outro))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value(startsWith("Acesso negado")));
    }

    @Test
    @DisplayName("body inválido devolve 400 com a mensagem de cada campo")
    void bodyInvalido() throws Exception {
        String token = novoUsuarioLogado();

        comToken(post("/api/transacoes"), token, "{'tipo':'X','valor':10,'categoriaId':%d}", categoriaId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.tipo").value("Valor inválido. Valores aceitos: R, D"));

        comToken(post("/api/transacoes"), token, "{'tipo':'D','valor':0}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(JSON_UTF8))
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos.valor").value("Valor deve ser maior que zero"))
                .andExpect(jsonPath("$.campos.categoriaId").value("Categoria é obrigatória"));

        mvc.perform(post("/api/transacoes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(startsWith("Corpo da requisição")));

        comToken(post("/api/orcamentos"), token, "{'categoriaId':%d,'mes':13,'ano':1999,'limite':100}", categoriaId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mes").value("Mês deve ser entre 1 e 12"))
                .andExpect(jsonPath("$.campos.ano").value("Ano deve ser entre 2000 e 2100"));
    }

    @Test
    @DisplayName("parâmetros inválidos na URL devolvem 400 com mensagem em português")
    void parametrosInvalidos() throws Exception {
        String token = novoUsuarioLogado();

        mvc.perform(get("/api/transacoes/mes?mes=13&ano=2026").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.mes").value("Mês deve ser entre 1 e 12"));

        mvc.perform(get("/api/transacoes/mes").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro obrigatório ausente: mes"));

        mvc.perform(get("/api/orcamentos/abc").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor inválido para 'id': abc. Informe um número"));
    }

    @Test
    @DisplayName("cadastro com senha curta devolve 400 no campo senha")
    void senhaCurta() throws Exception {
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'nome':'Usuário Teste','email':'%s','senha':'123'}", emailNovo())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").value("Senha deve ter entre 6 e 72 caracteres"));
    }

    private Integer outraCategoria() {
        return categoriaRepository.findAll().stream()
                .filter(categoria -> !categoria.getId().equals(categoriaId))
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNome("Lazer");
                    return categoriaRepository.save(categoria);
                })
                .getId();
    }

    private static String emailNovo() {
        return "teste-" + UUID.randomUUID().toString().substring(0, 8) + "@ex.com";
    }

    private static String json(String modelo, Object... valores) {
        return String.format(modelo, valores).replace('\'', '"');
    }

    private ResultActions registrar(String email) throws Exception {
        return mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                .content(json("{'nome':'Usuário Teste','email':'%s','senha':'%s'}", email, SENHA)));
    }

    private String login(String email) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'email':'%s','senha':'%s'}", email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuarioId").isNumber())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private String novoUsuarioLogado() throws Exception {
        String email = emailNovo();
        registrar(email).andExpect(status().isCreated());
        return login(email);
    }

    private ResultActions comToken(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
                                   String token, String modelo, Object... valores) throws Exception {
        return mvc.perform(requisicao.header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(modelo, valores)));
    }
}
