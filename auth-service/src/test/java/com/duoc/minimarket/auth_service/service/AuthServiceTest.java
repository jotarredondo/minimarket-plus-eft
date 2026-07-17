package com.duoc.minimarket.auth_service.service;

import com.duoc.minimarket.auth_service.dto.AuthResponse;
import com.duoc.minimarket.auth_service.dto.LoginRequest;
import com.duoc.minimarket.auth_service.dto.RegisterRequest;
import com.duoc.minimarket.auth_service.dto.UsuarioResponse;
import com.duoc.minimarket.auth_service.entity.Rol;
import com.duoc.minimarket.auth_service.entity.Usuario;
import com.duoc.minimarket.auth_service.exception.CredencialesInvalidasException;
import com.duoc.minimarket.auth_service.exception.EmailDuplicadoException;
import com.duoc.minimarket.auth_service.repository.UsuarioRepository;
import com.duoc.minimarket.auth_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "cliente@minimarket.cl";
    private static final String PASSWORD = "Cliente123";
    private static final String PASSWORD_ENCRIPTADA = "$2a$10$passwordEncriptada";
    private static final LocalDateTime FECHA_CREACION =
            LocalDateTime.of(2026, 7, 16, 20, 0);

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioCliente;

    @BeforeEach
    void configurarUsuario() {
        usuarioCliente = Usuario.builder()
                .id(1L)
                .nombre("Cliente Prueba")
                .email(EMAIL)
                .password(PASSWORD_ENCRIPTADA)
                .rol(Rol.ROLE_CLIENTE)
                .activo(true)
                .fechaCreacion(FECHA_CREACION)
                .build();
    }

    @Test
    void registrar_debeGuardarClienteConPasswordEncriptada() {
        RegisterRequest request = new RegisterRequest(
                "  Cliente Prueba  ",
                "  CLIENTE@MINIMARKET.CL  ",
                PASSWORD
        );

        when(usuarioRepository.existsByEmailIgnoreCase(EMAIL))
                .thenReturn(false);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(PASSWORD_ENCRIPTADA);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(1L);
                    usuario.setFechaCreacion(FECHA_CREACION);
                    return usuario;
                });

        UsuarioResponse response = authService.registrar(request);

        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioGuardado = usuarioCaptor.getValue();

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("Cliente Prueba", response.nombre()),
                () -> assertEquals(EMAIL, response.email()),
                () -> assertEquals(Rol.ROLE_CLIENTE, response.rol()),
                () -> assertTrue(response.activo()),
                () -> assertEquals(
                        PASSWORD_ENCRIPTADA,
                        usuarioGuardado.getPassword()
                ),
                () -> assertEquals(
                        Rol.ROLE_CLIENTE,
                        usuarioGuardado.getRol()
                ),
                () -> assertFalse(
                        usuarioGuardado.getPassword().equals(PASSWORD)
                )
        );

        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void registrar_debeLanzarExcepcionCuandoEmailYaExiste() {
        RegisterRequest request = new RegisterRequest(
                "Cliente Prueba",
                EMAIL,
                PASSWORD
        );

        when(usuarioRepository.existsByEmailIgnoreCase(EMAIL))
                .thenReturn(true);

        assertThrows(
                EmailDuplicadoException.class,
                () -> authService.registrar(request)
        );

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void login_debeGenerarJwtCuandoCredencialesSonCorrectas() {
        LoginRequest request = new LoginRequest(
                "  CLIENTE@MINIMARKET.CL  ",
                PASSWORD
        );

        UserDetails userDetails = User.builder()
                .username(EMAIL)
                .password(PASSWORD_ENCRIPTADA)
                .authorities(Rol.ROLE_CLIENTE.name())
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(usuarioCliente));

        when(jwtService.generarToken(userDetails))
                .thenReturn("jwt-token-prueba");

        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertAll(
                () -> assertEquals(
                        "jwt-token-prueba",
                        response.token()
                ),
                () -> assertEquals("Bearer", response.tipo()),
                () -> assertEquals(
                        3600L,
                        response.expiresInSeconds()
                ),
                () -> assertEquals(
                        EMAIL,
                        response.usuario().email()
                ),
                () -> assertEquals(
                        Rol.ROLE_CLIENTE,
                        response.usuario().rol()
                )
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService).generarToken(userDetails);
    }

    @Test
    void login_debeLanzarExcepcionCuandoCredencialesSonIncorrectas() {
        LoginRequest request = new LoginRequest(
                EMAIL,
                "PasswordIncorrecta"
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new BadCredentialsException("Credenciales incorrectas")
        );

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(request)
        );

        verify(usuarioRepository, never())
                .findByEmailIgnoreCase(any());

        verify(jwtService, never())
                .generarToken(any());
    }

    @Test
    void obtenerPerfil_debeRetornarUsuarioExistente() {
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(usuarioCliente));

        UsuarioResponse response =
                authService.obtenerPerfil(EMAIL);

        assertAll(
                () -> assertEquals(1L, response.id()),
                () -> assertEquals(
                        "Cliente Prueba",
                        response.nombre()
                ),
                () -> assertEquals(EMAIL, response.email()),
                () -> assertEquals(
                        Rol.ROLE_CLIENTE,
                        response.rol()
                ),
                () -> assertTrue(response.activo())
        );
    }

    @Test
    void obtenerPerfil_debeLanzarExcepcionCuandoUsuarioNoExiste() {
        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.obtenerPerfil(EMAIL)
        );
    }
}
