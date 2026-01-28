package com.myapp.reservations.controller;

import com.myapp.reservations.dto.userdto.AuthResponse;
import com.myapp.reservations.dto.SignInRequest;
import com.myapp.reservations.dto.userdto.UserRequest;
import com.myapp.reservations.dto.userdto.UserResponse;
import com.myapp.reservations.repository.UserRepository;
import com.myapp.reservations.service.UserService;
import com.myapp.reservations.security.JwtUtil;
import com.myapp.reservations.entities.User.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtils;
    private final UserService userService;

    @Autowired
    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtUtil jwtUtils,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }


    @PostMapping("/signin")
    public AuthResponse authenticateUser(@RequestBody SignInRequest user) {
        String login = user.identifier();


        if (login.contains("@")) {
            User existingUser = userRepository.findByEmail(login)
                    .orElseThrow(() -> new RuntimeException("Email not found"));

            login = existingUser.getName();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login,
                        user.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User authenticatedUser = userRepository.findByName(userDetails.getUsername());
        if(authenticatedUser == null){
            throw new RuntimeException("User not found");
        }

        String token = jwtUtils.generateToken(userDetails.getUsername(), authenticatedUser.getId());
        UserResponse userResponse = userService.findById(authenticatedUser.getId());

        return new AuthResponse(token, userResponse);
    }

    @PostMapping("/signup")
    public AuthResponse registerUser(@RequestBody UserRequest user) {
        if (userRepository.existsByName(user.name())||userRepository.existsByEmail(user.email())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        UserResponse newUser = userService.createUser(user);

        String token = jwtUtils.generateToken(user.name(), java.util.UUID.fromString(newUser.id().toString()));

        return new AuthResponse(token, newUser);
    }

    @GetMapping("/profile")
    public UserResponse getProfile() {
        java.util.UUID currentUserId = userService.getCurrentUserId();
        return userService.findById(currentUserId);
    }
}