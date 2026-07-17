package com.duoc.minimarket.auth_service.initializer;

import com.duoc.minimarket.auth_service.entity.Rol;
import com.duoc.minimarket.auth_service.entity.Usuario;
import com.duoc.minimarket.auth_service.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-email}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Value("${app.bootstrap.cashier-email}")
    private String cashierEmail;

    @Value("${app.bootstrap.cashier-password}")
    private String cashierPassword;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        crearUsuarioSiNoExiste(
                "Administrador MiniMarket Plus",
                adminEmail,
                adminPassword,
                Rol.ROLE_ADMIN
        );

        crearUsuarioSiNoExiste(
                "Cajero MiniMarket Plus",
                cashierEmail,
                cashierPassword,
                Rol.ROLE_CAJERO
        );
    }

    private void crearUsuarioSiNoExiste(
            String nombre,
            String email,
            String password,
            Rol rol
    ) {
        String emailNormalizado = email
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            logger.info(
                    "El usuario bootstrap {} ya existe",
                    emailNormalizado
            );
            return;
        }

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .email(emailNormalizado)
                .password(passwordEncoder.encode(password))
                .rol(rol)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        logger.info(
                "Usuario bootstrap creado: {} con rol {}",
                emailNormalizado,
                rol
        );
    }
}