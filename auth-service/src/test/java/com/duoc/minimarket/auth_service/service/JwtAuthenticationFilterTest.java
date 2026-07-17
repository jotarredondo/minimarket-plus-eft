package com.duoc.minimarket.auth_service.service;

import com.duoc.minimarket.auth_service.security.CustomUserDetailsService;
import com.duoc.minimarket.auth_service.security.JwtAuthenticationFilter;
import com.duoc.minimarket.auth_service.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL =
            "admin@minimarket.cl";

    private static final String TOKEN =
            "token-jwt-prueba";

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void configurarPrueba() {
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        userDetails = User.builder()
                .username(EMAIL)
                .password("password-encriptada")
                .authorities("ROLE_ADMIN")
                .build();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_debeContinuarCuandoNoExisteHeaderAuthorization()
            throws Exception {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verify(jwtService, never())
                .extraerEmail(org.mockito.ArgumentMatchers.anyString());

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
                .extraerEmail(org.mockito.ArgumentMatchers.anyString());

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

        when(jwtService.extraerEmail(TOKEN))
                .thenReturn(EMAIL);

        when(customUserDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(jwtService.esTokenValido(TOKEN, userDetails))
                .thenReturn(true);

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
        assertSame(userDetails, authentication.getPrincipal());
        assertEquals(EMAIL, authentication.getName());
        assertEquals(
                "ROLE_ADMIN",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(jwtService).extraerEmail(TOKEN);
        verify(customUserDetailsService)
                .loadUserByUsername(EMAIL);
        verify(jwtService)
                .esTokenValido(TOKEN, userDetails);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_noDebeAutenticarCuandoTokenEsInvalido()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer " + TOKEN
        );

        when(jwtService.extraerEmail(TOKEN))
                .thenReturn(EMAIL);

        when(customUserDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(jwtService.esTokenValido(TOKEN, userDetails))
                .thenReturn(false);

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

        verify(jwtService)
                .esTokenValido(TOKEN, userDetails);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_debeContinuarCuandoTokenProduceExcepcion()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer token-alterado"
        );

        when(jwtService.extraerEmail("token-alterado"))
                .thenThrow(new JwtException("Token inválido"));

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

        verify(customUserDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.anyString()
                );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_noDebeReemplazarAutenticacionExistente()
            throws Exception {

        var autenticacionExistente =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(autenticacionExistente);

        request.addHeader(
                "Authorization",
                "Bearer " + TOKEN
        );

        when(jwtService.extraerEmail(TOKEN))
                .thenReturn(EMAIL);

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

        verify(customUserDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.anyString()
                );

        verify(jwtService, never())
                .esTokenValido(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any()
                );

        verify(filterChain).doFilter(request, response);
    }
}
