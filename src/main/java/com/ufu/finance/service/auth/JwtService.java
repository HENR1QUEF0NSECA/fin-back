package com.ufu.finance.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Gera um token JWT com id e nome do usuário
     */
    public String generateToken(Long userId, String nome) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("nome", nome);
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrai todas as claims do token. Lança exceção se a assinatura for
     * inválida ou se o token estiver expirado.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Extrai o ID do usuário do token
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extrai o nome do usuário do token
     */
    public String extractNome(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("nome", String.class);
    }
    
    /**
     * Valida o token. O jjwt já rejeita token expirado ou com assinatura
     * inválida durante o parse, então basta tentar extrair as claims.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
