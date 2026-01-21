package iuh.fit.jwt.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import iuh.fit.jwt.model.User;

@Service
public class UserService implements UserDetailsService {
    private final Map<String, User> users = new HashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        initUsers(); // Khởi tạo users demo
    }

    /**
     * Khởi tạo demo users
     */
    public void initUsers() {
        // User thường
        users.put("user", new User(
                "user",
                passwordEncoder.encode("password"),
                "user@example.com",
                "Normal User",
                List.of("USER")
        ));

        // Admin
        users.put("admin", new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "admin@example.com",
                "Admin User",
                List.of("USER", "ADMIN")
        ));

        // Manager
        users.put("manager", new User(
                "manager",
                passwordEncoder.encode("manager123"),
                "manager@example.com",
                "Manager User",
                List.of("USER", "MANAGER")
        ));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return user;
    }


    /**
     * Authenticate user
     */
    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}
