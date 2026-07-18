package com.duoc.minimarket.catalog_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI catalogServiceOpenApi() {
        SecurityScheme jwtSecurityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "JWT obtenido desde auth-service. "
                                        + "Ingrese solamente el token."
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "MiniMarket Plus - Catalog Service"
                                )
                                .description(
                                        """
                                        API para gestionar categorías,
                                        productos, sucursales, inventario,
                                        movimientos de stock y órdenes
                                        de reposición.
                                        """
                                )
                                .version("1.0.0")
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_AUTH,
                                        jwtSecurityScheme
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(BEARER_AUTH)
                );
    }
}