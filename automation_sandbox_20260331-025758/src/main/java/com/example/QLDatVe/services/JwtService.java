package com.example.QLDatVe.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders; // <-- Import Decoders
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets; // Keep this if you ever switch back
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // --- 1. Read the correct property key ---
    @Value("${jwt.secret}") // <-- Changed from JWT_SECRET
    private String SECRET_KEY;

    // --- 2. Decode the Base64 key ---
    private SecretKey getSigningKey() {
        // Assuming the key in application.properties is Base64 encoded
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

        // If your key was plain text, you would use:
        // return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // ======== EXTRACT ========
    // (Extraction methods look correct for jjwt 0.12.x)

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Correct for 0.12.x
                .build()
                .parseSignedClaims(token)   // Correct for 0.12.x
                .getPayload();              // Correct for 0.12.x
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ======== GENERATE ========

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 1000L * 60 * 60 * 24); // 1 day expiration

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(exp)
                // --- 3. Correct signWith syntax ---
                .signWith(getSigningKey()) // <-- Algorithm is inferred from key in 0.12.x
                // ---------------------------------
                .compact();
    }

    // ======== VALIDATE ========
    // (Validation method looks correct)

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Also check if token is expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}