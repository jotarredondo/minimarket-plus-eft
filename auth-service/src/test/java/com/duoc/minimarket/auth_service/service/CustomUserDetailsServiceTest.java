package com.duoc.minimarket.auth_service.service;

import com.duoc.minimarket.auth_service.entity.Rol;
import com.duoc.minimarket.auth_service.entity.Usuario;
import com.duoc.minimarket.auth_service.repository.UsuarioRepository;
import com.duoc.minimarket.auth_service.security.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final String EMAIL =
            "admin@minimarket.cl";

    private static final String PASSWORD_ENCRIPTADA =
            "$2a$10$passwordEncriptada";

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_debeRetornarUsuarioActivoConRol() {

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Administrador MiniMarket")
                .email(EMAIL)
                .password(PASSWORD_ENCRIPTADA)
                .rol(Rol.ROLE_ADMIN)
                .activo(true)
                .build();

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                customUserDetailsService.loadUserByUsername(EMAIL);

        assertAll(
                () -> assertEquals(
                        EMAIL,
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        PASSWORD_ENCRIPTADA,
                        resultado.getPassword()
                ),
                () -> assertTrue(resultado.isEnabled()),
                () -> assertEquals(
                        1,
                        resultado.getAuthorities().size()
                ),
                () -> assertEquals(
                        Rol.ROLE_ADMIN.name(),
                        resultado.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority()
                )
        );

        verify(usuarioRepository)
                .findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void loadUserByUsername_debeDeshabilitarUsuarioInactivo() {

        Usuario usuarioInactivo = Usuario.builder()
                .id(2L)
                .nombre("Cajero Inactivo")
                .email("cajero@minimarket.cl")
                .password(PASSWORD_ENCRIPTADA)
                .rol(Rol.ROLE_CAJERO)
                .activo(false)
                .build();

        when(usuarioRepository.findByEmailIgnoreCase(
                "cajero@minimarket.cl"
        )).thenReturn(Optional.of(usuarioInactivo));

        UserDetails resultado =
                customUserDetailsService.loadUserByUsername(
                        "cajero@minimarket.cl"
                );

        assertAll(
                () -> assertEquals(
                        "cajero@minimarket.cl",
                        resultado.getUsername()
                ),
                () -> assertFalse(resultado.isEnabled()),
                () -> assertEquals(
                        Rol.ROLE_CAJERO.name(),
                        resultado.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority()
                )
        );
    }

    @Test
    void loadUserByUsername_debeLanzarExcepcionCuandoUsuarioNoExiste() {

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService
                        .loadUserByUsername(EMAIL)
        );

        assertTrue(
                exception.getMessage().contains(EMAIL)
        );

        verify(usuarioRepository)
                .findByEmailIgnoreCase(EMAIL);
    }
}
