package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET_BASE64 =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private static final String EMAIL =
            "admin@minimarket.cl";

    private static final String ROL =
            "ROLE_ADMIN";

    private JwtService jwtService;
    private SecretKey secretKey;

    @BeforeEach
    void configurarPrueba() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                SECRET_BASE64
        );

        byte[] keyBytes =
                Decoders.BASE64.decode(SECRET_BASE64);

        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void validarYExtraerClaims_debeRetornarClaimsDeTokenValido() {
        String token = generarToken(
                EMAIL,
                ROL,
                new Date(System.currentTimeMillis() + 60_000)
        );

        Claims claims =
                jwtService.validarYExtraerClaims(token);

        assertNotNull(claims);
        assertEquals(EMAIL, claims.getSubject());
        assertEquals(
                ROL,
                claims.get("rol", String.class)
        );
    }

    @Test
    void extraerEmail_debeRetornarSubjectDelToken() {
        String token = generarToken(
                EMAIL,
                ROL,
                new Date(System.currentTimeMillis() + 60_000)
        );

        String resultado =
                jwtService.extraerEmail(token);

        assertEquals(EMAIL, resultado);
    }

    @Test
    void extraerRol_debeRetornarRolDelToken() {
        String token = generarToken(
                EMAIL,
                ROL,
                new Date(System.currentTimeMillis() + 60_000)
        );

        String resultado =
                jwtService.extraerRol(token);

        assertEquals(ROL, resultado);
    }

    @Test
    void validarYExtraerClaims_debeRechazarTokenExpirado() {
        String tokenExpirado = generarToken(
                EMAIL,
                ROL,
                new Date(System.currentTimeMillis() - 60_000)
        );

        assertThrows(
                RuntimeException.class,
                () -> jwtService.validarYExtraerClaims(
                        tokenExpirado
                )
        );
    }

    @Test
    void validarYExtraerClaims_debeRechazarTokenAlterado() {
        String token = generarToken(
                EMAIL,
                ROL,
                new Date(System.currentTimeMillis() + 60_000)
        );

        String tokenAlterado =
                token.substring(0, token.length() - 4)
                        + "abcd";

        assertThrows(
                RuntimeException.class,
                () -> jwtService.validarYExtraerClaims(
                        tokenAlterado
                )
        );
    }

    private String generarToken(
            String email,
            String rol,
            Date expiracion
    ) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }
}