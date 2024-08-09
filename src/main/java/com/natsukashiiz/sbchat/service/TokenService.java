package com.natsukashiiz.sbchat.service;

import com.natsukashiiz.sbchat.common.TokenType;
import com.natsukashiiz.sbchat.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${api.jwt.issuer}")
    private String issuer;

    @Value("${api.jwt.expiration.access-token}")
    private Duration accessExpiration;

    @Value("${api.jwt.expiration.refresh-token}")
    private Duration refreshExpiration;

    public String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(accessExpiration))
                .id(RandomUtils.notSymbol())
                .subject(String.valueOf(userId))
                .claim("type", TokenType.Access.name())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(refreshExpiration))
                .id(RandomUtils.notSymbol())
                .subject(String.valueOf(userId))
                .claim("type", TokenType.Refresh.name())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return this.decoder.decode(token);
    }

    public boolean isAccessToken(Jwt jwt) {
        return Objects.equals(jwt.getClaim("type"), TokenType.Access.name());
    }

    public boolean isRefreshToken(Jwt jwt) {
        return Objects.equals(jwt.getClaim("type"), TokenType.Refresh.name());
    }

}
