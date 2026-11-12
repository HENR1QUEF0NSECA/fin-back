package com.ufu.finance.service.auth;

import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-usado-somente-nos-testes-automatizados-0123456789";

    private final JwtService jwtService = new JwtService(SEGREDO, 3_600_000);

    @Test
    void tokenGeradoEhValidoETrazIdENome() {
        String token = jwtService.generateToken(7L, "Ana");

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(7L);
        assertThat(jwtService.extractNome(token)).isEqualTo("Ana");
    }

    @Test
    void tokenAssinadoComOutroSegredoEhRecusado() {
        JwtService outro = new JwtService("outro-segredo-diferente-com-mais-de-32-caracteres", 3_600_000);

        assertThat(jwtService.validateToken(outro.generateToken(7L, "Ana"))).isFalse();
    }

    @Test
    void tokenExpiradoEhRecusado() {
        JwtService expiraNaHora = new JwtService(SEGREDO, -1_000);

        assertThat(jwtService.validateToken(expiraNaHora.generateToken(7L, "Ana"))).isFalse();
    }

    @Test
    void segredoCurtoImpedeAApiDeSubir() {
        assertThatThrownBy(() -> new JwtService("curto", 3_600_000))
                .isInstanceOf(WeakKeyException.class);
    }
}
