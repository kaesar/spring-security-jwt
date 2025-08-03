package com.example.security.domain.usecase;

import com.example.security.domain.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time-ms}")
    private long  expirationTime;

    @Value("${security.jwt.issuer}")
    private  String issuer;

    public String getToken(UserEntity userEntity) {
        return getToken(new HashMap<>(), userEntity);
    }

    private static final String SECRET_KEY="...";

    public String getToken(Map<String, Object> claims, UserEntity userEntity) {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        var key = Keys.hmacShaKeyFor(secretKeyBytes);

        return Jwts
                .builder()
                .setSubject(userEntity.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes=Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username=getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token));
    }

    /*private Claims getAllClaims(String token)
    {
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }*/

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver)
    {
        final Claims claims=getClaims(token);  // getAllClaims
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token)
    {
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token)
    {
        return getExpiration(token).before(new Date());
    }

    public Claims getClaims(String token) {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        var key = Keys.hmacShaKeyFor(secretKeyBytes);

        try {
            var claims = Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();

            Date due = claims.getExpiration();
            Date now = new Date(System.currentTimeMillis());

            if (now.before(due)) {
                return claims;
            }
        } catch (Exception e) {}
        return null;
    }
}
