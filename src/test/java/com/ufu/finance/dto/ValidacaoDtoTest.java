package com.ufu.finance.dto;

import com.ufu.finance.enums.TipoTransacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regras de validação dos DTOs de entrada, incluindo as anotações @Mes e @Ano.
 */
class ValidacaoDtoTest {

    private static ValidatorFactory fabrica;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validator = fabrica.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        fabrica.close();
    }

    @Test
    void orcamentoValidoNaoTemErros() {
        assertThat(erros(orcamento(5, 2026, "100.00"))).isEmpty();
    }

    @Test
    void mesEAnoForaDoIntervalo() {
        assertThat(erros(orcamento(13, 1999, "100.00"))).containsExactlyInAnyOrderEntriesOf(Map.of(
                "mes", "Mês deve ser entre 1 e 12",
                "ano", "Ano deve ser entre 2000 e 2100"));
        assertThat(erros(orcamento(0, 2101, "100.00"))).containsOnlyKeys("mes", "ano");
    }

    @Test
    void limitesDoAnoSaoAceitos() {
        assertThat(erros(orcamento(1, 2000, "100.00"))).isEmpty();
        assertThat(erros(orcamento(12, 2100, "100.00"))).isEmpty();
    }

    @Test
    void limiteDoOrcamentoPrecisaSerPositivo() {
        assertThat(erros(orcamento(5, 2026, "0"))).containsEntry("limite", "Limite deve ser maior que zero");
    }

    @Test
    void senhaComMenosDe6Caracteres() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Ana Teste");
        dto.setEmail("ana@ex.com");
        dto.setSenha("12345");

        assertThat(erros(dto)).containsExactly(Map.entry("senha", "Senha deve ter entre 6 e 72 caracteres"));
    }

    @Test
    void transacaoComValorComTresCasasDecimais() {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setTipo(TipoTransacao.D);
        dto.setValor(new BigDecimal("10.005"));
        dto.setCategoriaId(1);

        assertThat(erros(dto)).containsOnlyKeys("valor");
    }

    // ─── Auxiliares ───────────────────────────────────────────────────────────

    /** Erros de validação no formato campo → mensagem. */
    private static Map<String, String> erros(Object dto) {
        return validator.validate(dto).stream().collect(Collectors.toMap(
                (ConstraintViolation<Object> v) -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (primeira, segunda) -> primeira));
    }

    private static OrcamentoRequestDTO orcamento(int mes, int ano, String limite) {
        OrcamentoRequestDTO dto = new OrcamentoRequestDTO();
        dto.setCategoriaId(1);
        dto.setMes(mes);
        dto.setAno(ano);
        dto.setLimite(new BigDecimal(limite));
        return dto;
    }
}
