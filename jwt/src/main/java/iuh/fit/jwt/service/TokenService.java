package iuh.fit.jwt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Tạo Access Token
     * - Thời gian sống ngắn (15 phút)
     * - Chứa roles/authorities
     * - Dùng để access resources
     */
    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();

        // Lấy roles từ authentication
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // Bỏ prefix ROLE_
                .collect(Collectors.joining(" "));

        // Build JWT Claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self") // Issuer - ai tạo token
                .issuedAt(now) // Thời gian tạo
                .expiresAt(now.plus(accessTokenExpiration, ChronoUnit.MILLIS)) // Thời gian hết hạn
                .subject(authentication.getName()) // Subject - username
                .claim("roles", roles) // Custom claim - roles
                .claim("type", "access") // Token type
                .build();

        // Encode và return
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Tạo Refresh Token
     * - Thời gian sống dài (30 ngày)
     * - Không chứa roles (security)
     * - Chỉ dùng để lấy access token mới
     */
    public String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpiration, ChronoUnit.MILLIS))
                .subject(authentication.getName())
                .claim("type", "refresh") // Token type
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Validate token
     * - Decode token
     * - Kiểm tra signature
     * - Kiểm tra expiration
     */
    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now());
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Lấy username từ token
     */
    public String getUsernameFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    /**
     * Kiểm tra token type
     */
    public String getTokenType(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaim("type");
    }

    /**
     * Get access token expiration time in milliseconds
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
