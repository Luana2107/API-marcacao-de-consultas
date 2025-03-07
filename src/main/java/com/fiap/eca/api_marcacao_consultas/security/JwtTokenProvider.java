package com.fiap.eca.api_marcacao_consultas;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key chaveSecreta;

    @Value("${jwt.expiration}")
    private long tempoExpiracao;

    public JwtTokenProvider(@Value("${jwt.secret}") String segredo) {
        this.chaveSecreta = Keys.hmacShaKeyFor(segredo.getBytes());
    }

    // Gerar token JWT
    public String gerarToken(Authentication autenticacao) {
        String emailUsuario = autenticacao.getName();
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + tempoExpiracao);

        return Jwts.builder()
                .setSubject(emailUsuario)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(chaveSecreta, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extrair email do token
    public String obterEmailDoToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(chaveSecreta)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validar token JWT
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(chaveSecreta)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
