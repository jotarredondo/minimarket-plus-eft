package com.duoc.minimarket.auth_service.service;

import com.duoc.minimarket.auth_service.dto.RegisterRequest;
import com.duoc.minimarket.auth_service.dto.UsuarioResponse;
import com.duoc.minimarket.auth_service.entity.Rol;
import com.duoc.minimarket.auth_service.entity.Usuario;
import com.duoc.minimarket.auth_service.exception.EmailDuplicadoException;
import com.duoc.minimarket.auth_service.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegisterRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();

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

        Usuario guardado = usuarioRepository.save(usuario);

        return convertirAResponse(guardado);
    }

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
