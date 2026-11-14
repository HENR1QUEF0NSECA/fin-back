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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> ROTAS_PUBLICAS = Set.of("/api/auth/login", "/api/auth/registro");

    private final JwtService jwtService;

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

    private static String caminhoNormalizado(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return request.getServletPath() + (pathInfo != null ? pathInfo : "");
    }

    private void responderNaoAutorizado(HttpServletRequest request, HttpServletResponse response,
                                        String mensagem) {
        exceptionResolver.resolveException(request, response, null, new NaoAutenticadoException(mensagem));
    }
}
