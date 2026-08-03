package com.spring.luispa.ecommerce_api.integration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ChangePasswordRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProfileRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.domain.user.*;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.test.helpers.AddressTestHelper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private String accessToken;
    private Long userId;
    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        addressRepository.deleteAll();

        Role user = new Role(RoleName.ROLE_USER);
        Role admin = new Role(RoleName.ROLE_ADMIN);

        roleRepository.save(user);
        roleRepository.save(admin);

        // Register regular user
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Register admin user
        RegisterRequest adminRegisterRequest = new RegisterRequest();
        adminRegisterRequest.setEmail("admin@example.com");
        adminRegisterRequest.setPassword("admin123");
        adminRegisterRequest.setFirstName("Admin");
        adminRegisterRequest.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegisterRequest)))
                .andExpect(status().isCreated());

        User adminUser = userRepository.findByEmail("admin@example.com").orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        // Login as regular user to get the token
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponse jwtResponse = objectMapper.readValue(loginResponse, JwtResponse.class);
        accessToken = jwtResponse.getToken();
        userId = jwtResponse.getId();

        // Login as admin user to get the token
        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@example.com");
        adminLoginRequest.setPassword("admin123");

        String adminLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponse adminJwtResponse = objectMapper.readValue(adminLoginResponse, JwtResponse.class);
        adminAccessToken = adminJwtResponse.getToken();
    }

    // GET /api/users/me

    @Nested
    @DisplayName("GET /api/users/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current user profile when authenticated")
        void shouldReturnCurrentUserProfile_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.enabled").value(true));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isForbidden());
        }
    }

    // PUT /api/users/me

    @Nested
    @DisplayName("PUT /api/users/me")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile when authenticated")
        void shouldUpdateProfile_whenAuthenticated() throws Exception {
            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setFirstName("Updated");
            updateRequest.setLastName("Name");

            mockMvc.perform(put("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"));

            // Verify if the data was updated
            User updatedUser = userRepository.findById(userId).orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
            assertThat(updatedUser.getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            UpdateProfileRequest updateRequest = new UpdateProfileRequest();
            updateRequest.setFirstName("Updated");

            mockMvc.perform(put("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // POST /api/users/me/change-password

    @Nested
    @DisplayName("POST /api/users/me/change-password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password when current password is correct")
        void shouldChangePassword_whenCurrentPasswordIsCorrect() throws Exception {
            ChangePasswordRequest changeRequest = new ChangePasswordRequest();
            changeRequest.setCurrentPassword("password123");
            changeRequest.setNewPassword("newPassword456");

            mockMvc.perform(post("/api/users/me/change-password")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isOk());

            // Verify if the data was updated
            User updatedUser = userRepository.findById(userId).orElse(null);
            assertThat(updatedUser).isNotNull();
            assertThat(passwordEncoder.matches("newPassword456", updatedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("Should return 400 when current password is incorrect")
        void shouldReturn400_whenCurrentPasswordIsIncorrect() throws Exception {
            ChangePasswordRequest changeRequest = new ChangePasswordRequest();
            changeRequest.setCurrentPassword("wrongPassword");
            changeRequest.setNewPassword("newPassword456");

            mockMvc.perform(post("/api/users/me/change-password")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(changeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
        }
    }

    // ============================================================
    // GET /api/users/me/addresses
    // ============================================================

    @Nested
    @DisplayName("GET /api/users/me/addresses")
    class GetAddressesTests {

        @Test
        @DisplayName("Should return addresses when authenticated")
        void shouldReturnAddresses_whenAuthenticated() throws Exception {
            // Create address
            Address address = AddressTestHelper.newAddress();
            address.setUser(UserTestHelper.defaultUser(userId));
            addressRepository.save(address);

            mockMvc.perform(get("/api/users/me/addresses")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].street").value("Av. Reforma 123"));
        }

        @Test
        @DisplayName("Should return empty list when no addresses")
        void shouldReturnEmptyList_whenNoAddresses() throws Exception {
            mockMvc.perform(get("/api/users/me/addresses")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // POST /api/users/me/addresses

    @Nested
    @DisplayName("POST /api/users/me/addresses")
    class AddAddressTests {

        @Test
        @DisplayName("Should add address when authenticated")
        void shouldAddAddress_whenAuthenticated() throws Exception {
            AddAddressRequest addressRequest = new AddAddressRequest();
            addressRequest.setStreet("Av. Reforma 123");
            addressRequest.setAddressLine2("Some Street");
            addressRequest.setCity("Ciudad de México");
            addressRequest.setState("CDMX");
            addressRequest.setZipCode("06500");
            addressRequest.setCountry("México");
            addressRequest.setPhoneNumber("+52 55 1234 5678");
            addressRequest.setAddressType(AddressType.SHIPPING);
            addressRequest.setDeliveryInstruction("Leave with doorman");

            mockMvc.perform(post("/api/users/me/addresses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addressRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.street").value("Av. Reforma 123"))
                    .andExpect(jsonPath("$.city").value("Ciudad de México"))
                    .andExpect(jsonPath("$.defaultAddress").value(true));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            AddAddressRequest addressRequest = new AddAddressRequest();
            addressRequest.setStreet("Av. Reforma 123");

            mockMvc.perform(post("/api/users/me/addresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addressRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ADMIN: GET /api/users

    @Nested
    @DisplayName("GET /api/users (ADMIN)")
    class AdminGetUsersTests {

        @Test
        @DisplayName("Should return all users when admin is authenticated")
        void shouldReturnAllUsers_whenAdminAuthenticated() throws Exception {
            // Use admin user token
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + adminAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[*].email").exists())
                    .andExpect(jsonPath("$[?(@.email == 'test@example.com')]").exists())
                    .andExpect(jsonPath("$[?(@.email == 'admin@example.com')]").exists());
        }

        @Test
        @DisplayName("Should return 403 when user is not admin")
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            // Use regular user token
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401_whenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }
    }
}