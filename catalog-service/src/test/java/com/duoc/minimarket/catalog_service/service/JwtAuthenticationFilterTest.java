package com.duoc.minimarket.catalog_service.service;

import com.duoc.minimarket.catalog_service.security.JwtAuthenticationFilter;
import com.duoc.minimarket.catalog_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token-jwt-prueba";
    private static final String EMAIL = "admin@minimarket.cl";
    private static final String ROL = "ROLE_ADMIN";

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void configurarPrueba() {
        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_debeContinuarCuandoNoHayAuthorizationHeader()
            throws Exception {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verify(jwtService, never())
                .validarYExtraerClaims(anyString());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilter_debeContinuarCuandoHeaderNoEsBearer()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Basic credenciales"
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verify(jwtService, never())
                .validarYExtraerClaims(anyString());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilter_debeContinuarCuandoBearerNoContieneToken()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer    "
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verify(jwtService, never())
                .validarYExtraerClaims(anyString());

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilter_debeAutenticarCuandoTokenEsValido()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer " + TOKEN
        );

        when(jwtService.validarYExtraerClaims(TOKEN))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(EMAIL);

        when(claims.get("rol", String.class))
                .thenReturn(ROL);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        var authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertTrue(authentication.isAuthenticated());
        assertEquals(EMAIL, authentication.getPrincipal());
        assertEquals(EMAIL, authentication.getName());
        assertEquals(
                ROL,
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(jwtService)
                .validarYExtraerClaims(TOKEN);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_noDebeAutenticarCuandoTokenEsInvalido()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer token-alterado"
        );

        when(jwtService.validarYExtraerClaims("token-alterado"))
                .thenThrow(
                        new MalformedJwtException(
                                "Token inválido"
                        )
                );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_noDebeAutenticarCuandoFaltaElRol()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer " + TOKEN
        );

        when(jwtService.validarYExtraerClaims(TOKEN))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(EMAIL);

        when(claims.get("rol", String.class))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_noDebeReemplazarAutenticacionExistente()
            throws Exception {

        var autenticacionExistente =
                new UsernamePasswordAuthenticationToken(
                        "usuario-existente@minimarket.cl",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CAJERO"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(autenticacionExistente);

        request.addHeader(
                "Authorization",
                "Bearer " + TOKEN
        );

        when(jwtService.validarYExtraerClaims(TOKEN))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(EMAIL);

        when(claims.get("rol", String.class))
                .thenReturn(ROL);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertSame(
                autenticacionExistente,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }
}
