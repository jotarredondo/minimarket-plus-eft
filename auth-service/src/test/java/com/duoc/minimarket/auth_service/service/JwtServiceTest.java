package com.duoc.minimarket.auth_service.service;

import com.duoc.minimarket.auth_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET_BASE64 =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private static final long EXPIRATION_MS = 3_600_000L;

    private static final String EMAIL =
            "admin@minimarket.cl";

    private JwtService jwtService;

    private UserDetails adminUser;

    @BeforeEach
    void configurarPrueba() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                SECRET_BASE64
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expirationMs",
                EXPIRATION_MS
        );

        adminUser = User.builder()
                .username(EMAIL)
                .password("password-encriptada")
                .authorities("ROLE_ADMIN")
                .build();
    }

    @Test
    void generarToken_debeCrearJwtFirmado() {
        String token = jwtService.generarToken(adminUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generarToken_debeIncluirEmailYRol() {
        String token = jwtService.generarToken(adminUser);

        Claims claims = analizarToken(token);

        assertEquals(EMAIL, claims.getSubject());
        assertEquals(
                "ROLE_ADMIN",
                claims.get("rol", String.class)
        );
    }

    @Test
    void extraerEmail_debeRetornarEmailDelToken() {
        String token = jwtService.generarToken(adminUser);

        String emailExtraido =
                jwtService.extraerEmail(token);

        assertEquals(EMAIL, emailExtraido);
    }

    @Test
    void esTokenValido_debeRetornarTrueParaUsuarioCorrecto() {
        String token = jwtService.generarToken(adminUser);

        boolean resultado =
                jwtService.esTokenValido(token, adminUser);

        assertTrue(resultado);
    }

    @Test
    void esTokenValido_debeRetornarFalseParaUsuarioDistinto() {
        String token = jwtService.generarToken(adminUser);

        UserDetails otroUsuario = User.builder()
                .username("cliente@minimarket.cl")
                .password("password-encriptada")
                .authorities("ROLE_CLIENTE")
                .build();

        boolean resultado =
                jwtService.esTokenValido(token, otroUsuario);

        assertFalse(resultado);
    }

    @Test
    void getExpirationSeconds_debeConvertirMilisegundosASegundos() {
        long resultado =
                jwtService.getExpirationSeconds();

        assertEquals(3600L, resultado);
    }

    private Claims analizarToken(String token) {
        byte[] keyBytes =
                Decoders.BASE64.decode(SECRET_BASE64);

        SecretKey secretKey =
                Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
