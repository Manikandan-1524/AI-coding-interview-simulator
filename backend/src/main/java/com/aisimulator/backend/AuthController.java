package com.aisimulator.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static class AuthRequest {
        public String username;
        public String password;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.username).isPresent()) {
            return "Username already taken!";
        }

        User newUser = new User();
        newUser.setUsername(request.username);
        newUser.setPassword(passwordEncoder.encode(request.password)); // hashed, not plain text
        userRepository.save(newUser);

        return "Signup successful! You can now log in.";
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {
        var userOpt = userRepository.findByUsername(request.username);

        if (userOpt.isEmpty()) {
            return "No account found with that username.";
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            return "Incorrect password.";
        }

        return "Login successful! Welcome back, " + user.getUsername() + ".";
    }
}