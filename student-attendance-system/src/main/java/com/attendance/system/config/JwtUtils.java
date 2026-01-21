package com.attendance.system.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${attendance.app.jwtSecret}")
    private String jwtSecret;

    @Value("${attendance.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // --- EXISTING AUTHENTICATION METHODS ---

    public String generateTokenFromEmail(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log this for debugging if needed
            return false;
        }
    }

    // --- NEW QR ATTENDANCE METHODS ---

    /**
     * Generates a token specifically for the QR code.
     * Expiration is set to 60 seconds to prevent students from sharing static
     * screenshots.
     */
    public String generateQrToken(Long classId) {
        // 60,000 milliseconds = 1 minute
        int qrExpirationMs = 60000;

        return Jwts.builder()
                .setSubject("QR_ATTENDANCE_MARKER")
                .claim("classId", classId) // Store the class ID in the token
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + qrExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the classId from the scanned QR token after validation.
     */
    public Long getClassIdFromQrToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Retrieve the claim as a Long
        return claims.get("classId", Long.class);
    }
}