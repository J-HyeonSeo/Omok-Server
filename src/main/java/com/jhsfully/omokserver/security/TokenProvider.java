package com.jhsfully.omokserver.security;

import com.jhsfully.omokserver.dto.AuthDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    private static final String PLAYER_ID = "playerId";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;//1초 -> 1분 -> 1시간

    //Access 토큰 생성
    public String generateAccessToken(String playerId) {
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
        String playerId = getPlayerId(token);
        return new UsernamePasswordAuthenticationToken(new AuthDto(playerId), "", Collections.singleton(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    //회원 번호 가져오기.
    private String getPlayerId(String token) {
        return this.parseClaims(token).get(PLAYER_ID, String.class);
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
