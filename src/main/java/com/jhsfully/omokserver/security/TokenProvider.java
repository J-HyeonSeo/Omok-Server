package com.jhsfully.omokserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    private static final String PLAYER_ID = "playerId";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;//1초 -> 1분 -> 30분

    //Access 토큰 생성
    public String generateAccessToken(Long playerId) {
        ClaimsBuilder claims = Jwts.claims();
        claims.add(PLAYER_ID, playerId);

        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
            .claims(claims.build())
            .issuedAt(now)
            .expiration(expiredDate)
            .signWith(getSigningKey())
            .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //토큰을 통해, 인증 객체 생성.
    public Authentication getAuthentication(String token) {
        Long memberId = getPlayerId(token);
        return new UsernamePasswordAuthenticationToken(memberId, "");
    }

    //회원 번호 가져오기.
    private Long getPlayerId(String token) {
        return this.parseClaims(token).get(PLAYER_ID, Long.class);
    }

    //토큰 유효기간 검증.
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try{
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (MalformedJwtException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
