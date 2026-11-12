package com.ufu.finance.config;

import com.ufu.finance.service.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
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
            responderNaoAutorizado(response, "Token não fornecido");
            return;
        }

        String token = authHeader.substring(7);

        // Validar o token
        if (!jwtService.validateToken(token)) {
            responderNaoAutorizado(response, "Token inválido ou expirado");
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

    private void responderNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Sem o charset, o servlet escreve em ISO-8859-1 e o "ã"/"á" chegam
        // quebrados no front, que lê o JSON como UTF-8.
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"erro\": \"" + mensagem + "\"}");
    }
}
