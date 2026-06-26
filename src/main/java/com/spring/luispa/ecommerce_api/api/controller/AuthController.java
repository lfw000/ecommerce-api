package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefreshTokenRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.RefreshTokenResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.infrastructure.ratelimit.RateLimitType;
import com.spring.luispa.ecommerce_api.infrastructure.ratelimit.RateLimited;
import com.spring.luispa.ecommerce_api.security.JwtUtils;
import com.spring.luispa.ecommerce_api.services.AuthService;
import com.spring.luispa.ecommerce_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    @RateLimited(type = RateLimitType.REGISTER)
    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or email already exists")
    })
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @RateLimited(type = RateLimitType.LOGIN)
    @Operation(summary = "Authenticate user", description = "Login with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest request,
                                                        HttpServletRequest httRequest) {

        String clientIp = httRequest.getRemoteAddr();

        JwtResponse response = authService.authenticate(request, clientIp);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    @RateLimited(type = RateLimitType.GENERAL)
    @Operation(summary = "Refresh access token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httRequest) {

        String clientIp = httRequest.getRemoteAddr();

        RefreshTokenResponse response = authService.refreshToken(request, clientIp);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Refresh-Token", required = false) String refreshToken,
                                       HttpServletRequest httRequest) {

        String clientIp = httRequest.getRemoteAddr();

        authService.logout(refreshToken, clientIp);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
