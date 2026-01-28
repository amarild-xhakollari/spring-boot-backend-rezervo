package com.myapp.reservations.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    private final String jwtSecret ="1f360178aaa419e59d0feff13cc0f42ba4897ed477bc32c33c2fbb00e37db954";
    Key key =Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String username, UUID userId){
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+36000000))
                .signWith(key)
                .compact();
    }

    public String getUserFromToken(String token){
        return Jwts.parser().verifyWith((SecretKey) key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public UUID getUserIdFromToken(String token){
        Claims claims = Jwts.parser().verifyWith((SecretKey) key).build()
                .parseSignedClaims(token)
                .getPayload();
        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    public Boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            return true;
        }catch (Exception e){
            log.error("JWT  VALIDATION ERROR : {} " , e.getMessage());
        }
        return false;
    }
}
