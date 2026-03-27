package com.expenseflow.controller;

import com.expenseflow.model.User;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil,
                          UserDetailsService userDetailsService, UserRepository userRepository,
                          PasswordEncoder encoder) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.get("username"), req.get("password")));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(req.get("username"));
        return ResponseEntity.ok(Map.of(
            "token", jwtUtil.generateToken(userDetails),
            "username", userDetails.getUsername(),
            "roles", userDetails.getAuthorities()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        if (userRepository.existsByUsername(req.get("username")))
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        User user = new User();
        user.setUsername(req.get("username"));
        user.setPassword(encoder.encode(req.get("password")));
        user.setEmail(req.getOrDefault("email", req.get("username") + "@expenseflow.com"));
        user.setRoles(Set.of("EMPLOYEE"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }
}
