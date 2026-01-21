package iuh.fit.jwt.controller;

import iuh.fit.jwt.dto.LoginRequest;
import iuh.fit.jwt.dto.LoginResponse;
import iuh.fit.jwt.dto.RefreshTokenRequest;
import iuh.fit.jwt.model.User;
import iuh.fit.jwt.service.TokenService;
import iuh.fit.jwt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * POST /auth/login
     * Login endpoint - Trả về access token và refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Tạo Authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );

            // Generate tokens
            String accessToken = tokenService.generateAccessToken(authentication);
            String refreshToken = tokenService.generateRefreshToken(authentication);

            // Return response
            LoginResponse response = new LoginResponse(
                    accessToken,
                    refreshToken,
                    tokenService.getAccessTokenExpiration()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /auth/refresh
     * Refresh token endpoint - Lấy access token mới từ refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // Validate refresh token
            if (!tokenService.validateToken(refreshToken)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Kiểm tra token type
            String tokenType = tokenService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token is not a refresh token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Lấy username và load user
            String username = tokenService.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userService.loadUserByUsername(username);

            // Tạo Authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // Generate new access token
            String newAccessToken = tokenService.generateAccessToken(authentication);

            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", tokenService.getAccessTokenExpiration());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * POST /auth/validate
     * Validate token endpoint - Kiểm tra token có hợp lệ không
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        boolean isValid = tokenService.validateToken(token);

        response.put("valid", isValid);
        if (isValid) {
            response.put("username", tokenService.getUsernameFromToken(token));
            response.put("type", tokenService.getTokenType(token));
        }

        return ResponseEntity.ok(response);
    }
}
