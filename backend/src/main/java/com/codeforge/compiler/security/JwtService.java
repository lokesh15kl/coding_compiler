package com.codeforge.compiler.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties jwtProperties;
  private final SecretKey secretKey;

  public JwtService(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.secretKey = Keys.hmacShaKeyFor(deriveKey(jwtProperties.secret()));
  }

  public String generateToken(String subject, Map<String, Object> claims) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.expirationMinutes() * 60_000L);
    return Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, String subject) {
    return subject.equals(extractSubject(token)) && parseClaims(token).getExpiration().after(new Date());
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  private byte[] deriveKey(String secret) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("Unable to derive JWT signing key", exception);
    }
  }
}