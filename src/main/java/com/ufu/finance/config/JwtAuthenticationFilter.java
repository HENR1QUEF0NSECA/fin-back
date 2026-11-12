package com.ufu.finance.config;

import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    /**
     * O filtro roda antes dos controllers, fora do alcance do @RestControllerAdvice.
     * Este resolver entrega a exceção ao GlobalExceptionHandler, para o 401 sair
     * no mesmo formato JSON (e em UTF-8) de todas as outras respostas de erro.
     */
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Rotas públicas que não precisam de autenticação
        String path = request.getRequestURI();
        if (path.equals("/api/auth/login") || path.equals("/api/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrair o token do header Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            responderNaoAutorizado(request, response, "Token não fornecido");
            return;
        }

        String token = authHeader.substring(7);

        // Validar o token
        if (!jwtService.validateToken(token)) {
            responderNaoAutorizado(request, response, "Token inválido ou expirado");
            return;
        }

        // Extrair informações do usuário e adicionar ao request
        Long userId = jwtService.extractUserId(token);
        String nome = jwtService.extractNome(token);

        request.setAttribute("userId", userId);
        request.setAttribute("nome", nome);

        // Continuar com a requisição
        filterChain.doFilter(request, response);
    }

    private void responderNaoAutorizado(HttpServletRequest request, HttpServletResponse response,
                                        String mensagem) {
        exceptionResolver.resolveException(request, response, null, new NaoAutenticadoException(mensagem));
    }
}
