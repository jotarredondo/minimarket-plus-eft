package com.duoc.minimarket.auth_service.controller;

import com.duoc.minimarket.auth_service.dto.AuthResponse;
import com.duoc.minimarket.auth_service.dto.LoginRequest;
import com.duoc.minimarket.auth_service.dto.RegisterRequest;
import com.duoc.minimarket.auth_service.dto.UsuarioResponse;
import com.duoc.minimarket.auth_service.exception.ApiError;
import com.duoc.minimarket.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticación",
        description = "Registro, inicio de sesión y perfil del usuario autenticado"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Registrar cliente",
            description = """
                    Registra un nuevo usuario con el rol ROLE_CLIENTE.
                    La contraseña se almacena utilizando BCrypt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Cliente registrado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "El correo electrónico ya está registrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegisterRequest request
    ) {
        UsuarioResponse response = authService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Valida el correo y la contraseña del usuario.
                    Si las credenciales son correctas, devuelve un token JWT
                    junto con la información y el rol del usuario.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Autenticación correcta",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponse.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Formato de solicitud inválido",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Correo o contraseña incorrectos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar perfil autenticado",
            description = """
                    Obtiene la información del usuario identificado por el JWT.
                    Este endpoint requiere autenticación Bearer.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UsuarioResponse.class
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido o expirado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obtenerPerfil(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        UsuarioResponse response = authService.obtenerPerfil(
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }
}
