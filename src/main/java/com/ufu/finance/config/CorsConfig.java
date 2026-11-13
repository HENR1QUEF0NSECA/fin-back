package com.ufu.finance.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuração de CORS para fronts que chamam a API direto do navegador.
 *
 * O meu-norte-front (Next.js, localhost:3000) chama a API pelo servidor do Next,
 * então não depende disto; as origens ficam liberadas para chamadas feitas pelo
 * navegador durante o desenvolvimento.
 *
 * É registrado como filtro com prioridade máxima para rodar ANTES do
 * JwtAuthenticationFilter. Assim o preflight (OPTIONS, que o navegador manda
 * sem o header Authorization) é respondido aqui, e as respostas 401 do filtro
 * JWT também saem com os headers de CORS.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",      // outro front local (ex.: Angular)
                "http://localhost:3000",      // front Next.js
                "http://127.0.0.1:4200",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
