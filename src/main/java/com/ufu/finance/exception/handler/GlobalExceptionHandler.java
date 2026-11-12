package com.ufu.finance.exception.handler;

import com.ufu.finance.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ponto único de tratamento de erros da API.
 *
 * Toda resposta de erro sai daqui com o mesmo formato:
 * <pre>
 * { "status": 400, "erro": "mensagem", "campos": { "valor": "..." }, "timestamp": "..." }
 * </pre>
 * "campos" só aparece em erro de validação, com uma mensagem por campo.
 * O 401 do JwtAuthenticationFilter também passa por aqui.
 *
 * As mensagens são sempre em português e nunca expõem detalhes internos
 * (SQL, nomes de classes Java); esses detalhes vão só para o log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── Regras de negócio (lançadas pelos services) ──────────────────────────

    /** Exceções da própria API: o status vem do tipo (404, 409, 403...). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        return resposta(ex.getStatus(), ex.getMessage(), null);
    }

    // ─── Validação dos dados de entrada ───────────────────────────────────────

    /** Body com @Valid inválido: devolve uma mensagem por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBodyInvalido(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    /** Parâmetro de query/URL que falhou numa validação, ex.: ?mes=13 com @Mes. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleParametroInvalido(HandlerMethodValidationException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            String parametro = resultado.getMethodParameter().getParameterName();
            resultado.getResolvableErrors()
                    .forEach(erro -> campos.putIfAbsent(parametro, erro.getDefaultMessage()));
        });
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    /** Body ilegível: JSON mal formado, ausente, ou campo com tipo errado ("tipo": "X"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBodyIlegivel(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof MismatchedInputException erro) {
            String campo = ultimoCampo(erro);
            if (campo != null) {
                return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos",
                        Map.of(campo, "Valor inválido" + dica(erro.getTargetType())));
            }
        }
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição ausente ou mal formatado: envie um JSON válido", null);
    }

    /** Parâmetro obrigatório ausente, ex.: GET /api/transaction/mes sem ?mes= */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleParametroAusente(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + ex.getParameterName(), null);
    }

    /** Valor com tipo errado na URL, ex.: /api/orcamentos/abc ou /api/transaction/tipo/X */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para '" + ex.getName() + "': " + ex.getValue() + dica(ex.getRequiredType()),
                null);
    }

    // ─── Erros de rota / protocolo HTTP ───────────────────────────────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRotaInexistente(NoResourceFoundException ex) {
        return resposta(HttpStatus.NOT_FOUND,
                "Rota não encontrada: " + ex.getHttpMethod() + " /" + ex.getResourcePath(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        // Os headers trazem o "Allow" com os métodos aceitos pela rota.
        return resposta(ex.getStatusCode(),
                "Método " + ex.getMethod() + " não é suportado nesta rota", null, ex.getHeaders());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleContentTypeNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        return resposta(ex.getStatusCode(),
                "Content-Type não suportado: envie o corpo como application/json", null, ex.getHeaders());
    }

    // ─── Banco de dados e erros inesperados ───────────────────────────────────

    /** Restrição do banco (ex.: unique) que escapou das checagens dos services. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já existentes", null);
    }

    /**
     * Rede de segurança para o que não caiu nos handlers acima.
     * Exceções do próprio Spring (todas implementam ErrorResponse) mantêm o status delas.
     * Qualquer outra coisa é bug ou falha de infraestrutura: 500, com o detalhe só no log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInesperado(Exception ex) {
        if (ex instanceof ErrorResponse erroSpring) {
            HttpStatusCode status = erroSpring.getStatusCode();
            return resposta(status, mensagemPadrao(status), null, erroSpring.getHeaders());
        }
        log.error("Erro não tratado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor. Tente novamente mais tarde.", null);
    }

    // ─── Montagem da resposta ─────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String erro,
                                                         Map<String, String> campos) {
        return resposta(status, erro, campos, HttpHeaders.EMPTY);
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatusCode status, String erro,
                                                         Map<String, String> campos, HttpHeaders headers) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("erro", erro);
        if (campos != null && !campos.isEmpty()) {
            corpo.put("campos", campos);
        }
        corpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).headers(headers).body(corpo);
    }

    private static String mensagemPadrao(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 406 -> "Formato de resposta não suportado: use application/json";
            default -> status.is5xxServerError()
                    ? "Erro interno do servidor. Tente novamente mais tarde."
                    : "Não foi possível processar a requisição";
        };
    }

    /** Nome do campo do JSON onde o Jackson falhou, ex.: "tipo". */
    private static String ultimoCampo(JacksonException erro) {
        List<JacksonException.Reference> caminho = erro.getPath();
        for (int i = caminho.size() - 1; i >= 0; i--) {
            String nome = caminho.get(i).getPropertyName();
            if (nome != null) {
                return nome;
            }
        }
        return null;
    }

    /** Complemento da mensagem com o formato esperado para o tipo do campo. */
    private static String dica(Class<?> tipo) {
        if (tipo == null) {
            return "";
        }
        if (tipo.isEnum()) {
            return ". Valores aceitos: " + Arrays.stream(tipo.getEnumConstants())
                    .map(constante -> ((Enum<?>) constante).name())
                    .collect(Collectors.joining(", "));
        }
        if (tipo == LocalDate.class) {
            return ". Use o formato AAAA-MM-DD";
        }
        if (Number.class.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(tipo))) {
            return ". Informe um número";
        }
        return "";
    }
}
