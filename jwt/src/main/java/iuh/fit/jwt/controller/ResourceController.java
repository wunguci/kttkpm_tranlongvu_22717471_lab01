package iuh.fit.jwt.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ResourceController {

    /**
     * Public endpoint - Không cần authentication
     */
    @GetMapping ("/public/hello")
    public Map<String, String> publicHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from public endpoint!");
        response.put("description", "This endpoint is accessible without authentication");
        return response;
    }

    /**
     * User endpoint - Cần role USER hoặc ADMIN
     */
    @GetMapping("/user/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> userProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * User dashboard - Cần role USER
     */
    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> userDashboard(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to User Dashboard");
        response.put("user", authentication.getName());
        return response;
    }

    /**
     * Admin endpoint - Chỉ ADMIN mới access được
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminGetUsers(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin: List of all users");
        response.put("admin", authentication.getName());
        response.put("users", new String[]{"user1", "user2", "admin"});
        return response;
    }

    /**
     * Admin dashboard - Chỉ ADMIN
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminDashboard(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to Admin Dashboard");
        response.put("admin", authentication.getName());
        response.put("privileges", "Full access to all resources");
        return response;
    }

    /**
     * Protected endpoint - Bất kỳ ai authenticated
     */
    @GetMapping("/protected")
    public Map<String, Object> protectedResource(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected resource");
        response.put("user", authentication.getName());
        response.put("authenticated", authentication.isAuthenticated());
        return response;
    }
}
