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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registra públicamente un nuevo usuario.
     * Por seguridad, todo registro público recibe el rol CLIENTE.
     */
    @Transactional
    public UsuarioResponse registrar(RegisterRequest request) {

        String emailNormalizado = request.email()
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new EmailDuplicadoException(emailNormalizado);
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .email(emailNormalizado)
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.ROLE_CLIENTE)
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return convertirAResponse(usuarioGuardado);
    }

    /**
     * Valida las credenciales mediante Spring Security
     * y genera un token JWT cuando son correctas.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String emailNormalizado = request.email()
                .trim()
                .toLowerCase();

        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            emailNormalizado,
                            request.password()
                    )
            );

            UserDetails userDetails =
                    (UserDetails) authentication.getPrincipal();

            Usuario usuario = usuarioRepository
                    .findByEmailIgnoreCase(emailNormalizado)
                    .orElseThrow(CredencialesInvalidasException::new);

            String token = jwtService.generarToken(userDetails);

            return new AuthResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationSeconds(),
                    convertirAResponse(usuario)
            );

        } catch (AuthenticationException exception) {
            throw new CredencialesInvalidasException();
        }
    }

    /**
     * Obtiene la información del usuario autenticado.
     * El email proviene del Authentication de Spring Security.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String email) {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(CredencialesInvalidasException::new);

        return convertirAResponse(usuario);
    }

    /**
     * Convierte la entidad JPA en un DTO seguro.
     * La contraseña nunca se incluye en la respuesta.
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getFechaCreacion()
        );
    }
}