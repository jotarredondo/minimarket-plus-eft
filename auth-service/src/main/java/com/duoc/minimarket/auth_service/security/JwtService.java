package com.duoc.minimarket.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-ms}")
    private long expirationMs;

    public String generarToken(UserDetails userDetails) {
        String rol = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expirationMs)
                )
                .signWith(obtenerClave())
                .compact();
    }

    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public boolean esTokenValido(
            String token,
            UserDetails userDetails
    ) {
        String email = extraerEmail(token);

        return email.equalsIgnoreCase(userDetails.getUsername())
                && !estaExpirado(token);
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private boolean estaExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    private <T> T extraerClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        Claims claims = extraerTodosLosClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClave() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
