    package com.mygym.crm.trainercontributioncalculator.service.security.jwt;

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.security.Keys;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;

    import javax.crypto.SecretKey;

    @Component
    public class JwtService {

        private final SecretKey secretKey;

        public JwtService(@Value("${JWT_SECRET}") String secret) {
            if (secret == null || secret.isEmpty()) {
                throw new IllegalStateException("JWT_SECRET environment variable is not set!");
            }

            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        }

        public Claims parseToken(String token){
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
    }