package com.ufu.finance.config;

import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Set;

/**
 * Exige um token JWT válido em todas as rotas /api/*, exceto login e cadastro. O id e o nome do usuário vão para os atributos
 * "usuarioId" e "nome" da requisição, que os controllers leem com
 * {@code @RequestAttribute}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Rotas que dispensam token. Comparadas com o caminho já normalizado pelo servidor. */
    private static final Set<String> ROTAS_PUBLICAS = Set.of("/api/auth/login", "/api/auth/registro");

    private final JwtService jwtService;

    /**
     * O filtro roda antes dos controllers, fora do alcance do @RestControllerAdvice.
     * Este resolver entrega a exceção ao GlobalExceptionHandler, para o 401 sair
     * no mesmo formato JSON (e em UTF-8) de todas as outras respostas de erro.
     */
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (ROTAS_PUBLICAS.contains(caminhoNormalizado(request))) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            responderNaoAutorizado(request, response, "Token não fornecido");
            return;
        }

        String token = authHeader.substring(7);
        Long usuarioId = jwtService.validateToken(token) ? jwtService.extractUserId(token) : null;
        if (usuarioId == null) {
            responderNaoAutorizado(request, response, "Token inválido ou expirado");
            return;
        }

        request.setAttribute("usuarioId", usuarioId);
        request.setAttribute("nome", jwtService.extractNome(token));

        filterChain.doFilter(request, response);
    }

    /**
     * Caminho da requisição já decodificado e sem "..", ao contrário do
     * getRequestURI(). Assim "/api/auth/../transacoes" não passa como rota pública.
     */
    private static String caminhoNormalizado(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return request.getServletPath() + (pathInfo != null ? pathInfo : "");
    }

    private void responderNaoAutorizado(HttpServletRequest request, HttpServletResponse response,
                                        String mensagem) {
        exceptionResolver.resolveException(request, response, null, new NaoAutenticadoException(mensagem));
    }
}
