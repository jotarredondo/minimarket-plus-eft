package com.duoc.minimarket.auth_service.security;

import com.duoc.minimarket.auth_service.entity.Usuario;
import com.duoc.minimarket.auth_service.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con el email: " + email
                        )
                );

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(usuario.getRol().name())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
