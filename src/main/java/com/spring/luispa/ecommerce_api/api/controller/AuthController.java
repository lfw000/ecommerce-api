package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.security.JwtUtils;
import com.spring.luispa.ecommerce_api.services.AuthService;
import com.spring.luispa.ecommerce_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          AuthService authService,
                          JwtUtils jwtUtils) {
        this.authService = authService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Login with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content =  @Content),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.authenticate(request);

        return ResponseEntity.status(HttpStatus.OK).body(jwtResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email already registered or invalid data")
    })
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
