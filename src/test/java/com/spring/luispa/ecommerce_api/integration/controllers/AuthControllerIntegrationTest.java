package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefreshTokenRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.domain.user.Role;
import com.spring.luispa.ecommerce_api.domain.user.RoleRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.test.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        Role user = new Role(RoleName.ROLE_USER);
        Role admin = new Role(RoleName.ROLE_ADMIN);

        roleRepository.save(user);
        roleRepository.save(admin);
    }

    // POST /api/auth/register

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register user when email is available")
        void shouldRegisterUser_whenEmailIsAvailable() throws Exception {
            String responseJson = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.enabled").value(true))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Check if the data was saved on the database
            User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("Should return 409 when email already exists")
        void shouldReturn400_whenEmailAlreadyExists() throws Exception {
            // Create an existing user
            User existingUser = UserTestHelper.defaultUser();
            existingUser.setEmail("test@example.com");
            existingUser.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(existingUser);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400_whenEmailIsMissing() throws Exception {
            registerRequest.setEmail(null);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400_whenPasswordIsTooShort() throws Exception {
            registerRequest.setPassword("123");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    // POST /api/auth/login

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully when credentials are valid")
        void shouldLoginSuccessfully_whenCredentialsAreValid() throws Exception {
            // Create a user on the database
            User user = UserTestHelper.defaultUser();
            user.setEmail("test@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(user);

            String responseJson = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // Verify token
            JwtResponse response = objectMapper.readValue(responseJson, JwtResponse.class);
            assertThat(response.getToken()).isNotNull();
            assertThat(response.getRefreshToken()).isNotNull();
        }

        @Test
        @DisplayName("Should return 401 when password is incorrect")
        void shouldReturn401_whenPasswordIsIncorrect() throws Exception {
            // Create a user on the database
            User user = UserTestHelper.defaultUser();
            user.setEmail("test@example.com");
            user.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(user);

            loginRequest.setPassword("wrongpassword");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("Should return 401 when email does not exist")
        void shouldReturn401_whenEmailDoesNotExist() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
        }
    }

    // POST /api/auth/refresh

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully when refresh token is valid")
        void shouldRefreshTokenSuccessfully_whenRefreshTokenIsValid() throws Exception {
            // Register user
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            // Login to get a refresh token
            String loginResponse = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JwtResponse jwtResponse = objectMapper.readValue(loginResponse, JwtResponse.class);
            String refreshToken = jwtResponse.getRefreshToken();

            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(refreshToken);

            // Refresh token
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.expiresIn").exists());
        }

        @Test
        @DisplayName("Should return 400 when refresh token is invalid")
        void shouldReturn400_whenRefreshTokenIsInvalid() throws Exception {
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken("invalid-refresh-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"));
        }

        @Test
        @DisplayName("Should return 400 when refresh token is missing")
        void shouldReturn400_whenRefreshTokenIsMissing() throws Exception {
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(null);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
    }

    // POST /api/auth/logout

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully when refresh token is provided")
        void shouldLogoutSuccessfully_whenRefreshTokenIsProvided() throws Exception {
            // Register and login
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            String loginResponse = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JwtResponse jwtResponse = objectMapper.readValue(loginResponse, JwtResponse.class);

            // Logout
            mockMvc.perform(post("/api/auth/logout")
                            .header("Refresh-Token", jwtResponse.getRefreshToken()))
                    .andExpect(status().isOk());

            // Try to refresh with the same token (should fail)
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefreshToken(jwtResponse.getRefreshToken());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"));
        }

        @Test
        @DisplayName("Should return 200 even when refresh token is not provided")
        void shouldReturn200_whenRefreshTokenIsNotProvided() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk());
        }
    }
}