package com.duoc.minimarket.auth_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "MiniMarket Plus - Auth Service",
                version = "1.0.0",
                description = """
                        Microservicio encargado del registro de clientes,
                        autenticación de usuarios y generación de tokens JWT
                        para MiniMarket Plus.
                        """,
                contact = @Contact(
                        name = "Equipo MiniMarket Plus"
                ),
                license = @License(
                        name = "Proyecto académico DUOC UC"
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Token JWT generado mediante POST /api/auth/login",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

public class OpenApiConfig {
}
