package com.spring.luispa.ecommerce_api.unit.services;

import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefreshTokenRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.RefreshTokenResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.RefreshToken;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.security.JwtUtils;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.services.AuthService;
import com.spring.luispa.ecommerce_api.services.RefreshTokenService;
import com.spring.luispa.ecommerce_api.services.UserService;
import com.spring.luispa.ecommerce_api.shared.exception.InvalidCredentialsException;
import com.spring.luispa.ecommerce_api.test.helpers.UserTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    // Mocks

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoggingAspect loggingAspect;

    private AuthService authService;

    // Test data

    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private User testUser;
    private UserResponse userResponse;
    private RefreshToken refreshToken;
    private String clientIp = "192.168.1.100";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                jwtUtils,
                userService,
                refreshTokenService,
                loggingAspect
        );

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token-123");

        testUser = UserTestHelper.defaultUser(1L);
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-123");
        refreshToken.setUser(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clear security context
        SecurityContextHolder.clearContext();
    }

    // Authentication tests

    @Nested
    @DisplayName("Authentication (Login) Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should authenticate successfully when credentials are valid")
        void shouldAuthenticate_whenCredentialsAreValid() {
            // Create Spring Security mocks
            Authentication authentication = mock(Authentication.class);
            UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getId()).thenReturn(1L);
            when(userDetails.getEmail()).thenReturn("test@example.com");

            when(jwtUtils.generateJwtToken(authentication)).thenReturn("access-token-123");
            when(userService.findById(1L)).thenReturn(userResponse);
            when(refreshTokenService.createRefreshToken(1L, clientIp)).thenReturn(refreshToken);

            // Execute method
            JwtResponse result = authService.authenticate(loginRequest, clientIp);

            // Verify
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("access-token-123");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token-123");
            assertThat(result.getId()).isEqualTo(1L);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils).generateJwtToken(authentication);
            verify(refreshTokenService).createRefreshToken(1L, clientIp);
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when credentials are invalid")
        void shouldThrowInvalidCredentialsException_whenCredentialsAreInvalid() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThatThrownBy(() -> authService.authenticate(loginRequest, clientIp))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid email or password");

            verify(jwtUtils, never()).generateJwtToken(any());
            verify(refreshTokenService, never()).createRefreshToken(any(), any());
        }
    }

    // Refresh token tests

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully when refresh token is valid")
        void shouldRefreshToken_whenRefreshTokenIsValid() {
            // Configure mocks
            when(refreshTokenService.verify("refresh-token-123", clientIp)).thenReturn(refreshToken);
            when(jwtUtils.generateTokenFromEmail("test@example.com")).thenReturn("new-access-token-456");
            when(refreshTokenService.createRefreshToken(1L, clientIp)).thenReturn(
                    new RefreshToken("new-refresh-token-789", testUser, null, clientIp)
            );
            when(jwtUtils.getJwtExpirationMs()).thenReturn(900000);

            // Execute method
            RefreshTokenResponse result = authService.refreshToken(refreshTokenRequest, clientIp);

            // Verify
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("new-access-token-456");
            assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token-789");
            assertThat(result.getExpiresIn()).isEqualTo(900);

            verify(refreshTokenService).verify("refresh-token-123", clientIp);
            verify(jwtUtils).generateTokenFromEmail("test@example.com");
            verify(refreshTokenService).createRefreshToken(1L, clientIp);
            verify(refreshTokenService).revoke("refresh-token-123", clientIp);
        }

        @Test
        @DisplayName("Should throw exception when refresh token is invalid")
        void shouldThrowException_whenRefreshTokenIsInvalid() {
            when(refreshTokenService.verify("refresh-token-123", clientIp))
                    .thenThrow(new RuntimeException("Invalid refresh token"));

            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest, clientIp))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid refresh token");

            verify(jwtUtils, never()).generateTokenFromEmail(any());
            verify(refreshTokenService, never()).createRefreshToken(any(), any());
        }
    }

    // Logout tests

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully when refresh token is provided")
        void shouldLogout_whenRefreshTokenIsProvided() {
            authService.logout("refresh-token-123", clientIp);

            verify(refreshTokenService).revoke("refresh-token-123", clientIp);
        }

        @Test
        @DisplayName("Should do nothing when refresh token is null")
        void shouldDoNothing_whenRefreshTokenIsNull() {
            authService.logout(null, clientIp);

            verify(refreshTokenService, never()).revoke(any(), any());
        }

        @Test
        @DisplayName("Should do nothing when refresh token is blank")
        void shouldDoNothing_whenRefreshTokenIsBlank() {
            authService.logout("   ", clientIp);

            verify(refreshTokenService, never()).revoke(any(), any());
        }
    }
}