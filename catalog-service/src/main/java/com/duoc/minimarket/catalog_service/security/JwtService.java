package com.duoc.minimarket.catalog_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    /**
     * Valida la firma y la expiración del JWT.
     * Si el token es inválido o está vencido, JJWT lanza una excepción.
     */
    public Claims validarYExtraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerEmail(String token) {
        return validarYExtraerClaims(token)
                .getSubject();
    }

    public String extraerRol(String token) {
        return validarYExtraerClaims(token)
                .get("rol", String.class);
    }

    private SecretKey obtenerClave() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
