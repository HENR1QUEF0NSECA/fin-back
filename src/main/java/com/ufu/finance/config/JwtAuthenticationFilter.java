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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\": \"Token não fornecido\"}");
            return;
        }
        
        String token = authHeader.substring(7);
        
        // Validar o token
        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"erro\": \"Token inválido ou expirado\"}");
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
}
