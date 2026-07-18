package com.duoc.minimarket.sales_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH =
            "bearerAuth";

    @Bean
    public OpenAPI salesServiceOpenApi() {
        SecurityScheme jwtSecurityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "JWT generado por auth-service. "
                                        + "Ingrese solamente el token."
                        );

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "MiniMarket Plus - Sales Service"
                                )
                                .description(
                                        """
                                        API para la gestión de carritos,
                                        pedidos, ventas, promociones
                                        y reportes comerciales.
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