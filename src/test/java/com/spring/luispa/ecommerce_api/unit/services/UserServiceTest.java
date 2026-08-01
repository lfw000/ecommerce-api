package com.spring.luispa.ecommerce_api.unit.services;

import com.spring.luispa.ecommerce_api.api.dto.request.AddAddressRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ChangePasswordRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RegisterRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateProfileRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.AddressResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.*;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.AddressMapper;
import com.spring.luispa.ecommerce_api.mappers.UserMapper;
import com.spring.luispa.ecommerce_api.services.UserService;
import com.spring.luispa.ecommerce_api.shared.enums.RoleName;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.DuplicateResourceException;
import com.spring.luispa.ecommerce_api.shared.exception.MissingDefaultRoleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.test.helpers.AddressTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    // Mocks

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoggingAspect loggingAspect;

    private UserService userService;

    // Test data

    private User testUser;
    private UserResponse testResponse;
    private Address testAddress;
    private AddressResponse testAddressResponse;
    private RegisterRequest registerRequest;
    private UpdateProfileRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;
    private AddAddressRequest addAddressRequest;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                roleRepository,
                addressRepository,
                userMapper,
                addressMapper,
                passwordEncoder,
                loggingAspect
        );

        testUser = UserTestHelper.defaultUser(1L);

        testResponse = new UserResponse();
        testResponse.setId(1L);
        testResponse.setEmail("test@example.com");
        testResponse.setFirstName("Test");
        testResponse.setLastName("User");

        testAddress = AddressTestHelper.defaultAddress(1L, testUser);
        testAddressResponse = new AddressResponse();
        testAddressResponse.setId(1L);
        testAddressResponse.setStreet("Av. Reforma 123");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("oldPassword");
        changePasswordRequest.setNewPassword("newPassword123");

        addAddressRequest = new AddAddressRequest();
        addAddressRequest.setStreet("Av. Reforma 123");
        addAddressRequest.setCity("Ciudad de México");
        addAddressRequest.setState("CDMX");
        addAddressRequest.setZipCode("06500");
        addAddressRequest.setCountry("México");
        addAddressRequest.setAddressType(AddressType.SHIPPING);

        userRole = new Role(RoleName.ROLE_USER);
    }

    // Registration tests

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user when email is available")
        void shouldRegisterUser_whenEmailIsAvailable() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(userMapper.toEntity(registerRequest)).thenReturn(testUser);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowException_whenEmailAlreadyExists() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already registered");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when default role not found")
        void shouldThrowException_whenDefaultRoleNotFound() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(userMapper.toEntity(registerRequest)).thenReturn(testUser);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.register(registerRequest))
                    .isInstanceOf(MissingDefaultRoleException.class)
                    .hasMessageContaining("Default role not configured");
            verify(userRepository, never()).save(any());
        }
    }

    // Profile management tests

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {

        @Test
        @DisplayName("Should update profile when user exists")
        void shouldUpdateProfile_whenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.updateProfile(1L, updateRequest);

            assertThat(result).isNotNull();
            assertThat(testUser.getFirstName()).isEqualTo("Updated");
            assertThat(testUser.getLastName()).isEqualTo("Name");
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found for profile update")
        void shouldThrowException_whenUserNotFoundForProfileUpdate() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfile(999L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should change password when current password matches")
        void shouldChangePassword_whenCurrentPasswordMatches() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

            userService.changePassword(1L, changePasswordRequest);

            assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowException_whenCurrentPasswordIncorrect() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, changePasswordRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Current password is incorrect");
        }

        @Test
        @DisplayName("Should throw exception when user not found for password change")
        void shouldThrowException_whenUserNotFoundForPasswordChange() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(999L, changePasswordRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Role tests

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should add role to user")
        void shouldAddRoleToUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(new Role(RoleName.ROLE_ADMIN)));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.addRole(1L, RoleName.ROLE_ADMIN);

            assertThat(result).isNotNull();
            assertThat(testUser.getRoles()).contains(new Role(RoleName.ROLE_ADMIN));
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found for role add")
        void shouldThrowException_whenUserNotFoundForRoleAdd() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.addRole(999L, RoleName.ROLE_ADMIN))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when role not found")
        void shouldThrowException_whenRoleNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.addRole(1L, RoleName.ROLE_ADMIN))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRoleFromUser() {
            testUser.addRole(new Role(RoleName.ROLE_ADMIN));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(new Role(RoleName.ROLE_ADMIN)));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.removeRole(1L, RoleName.ROLE_ADMIN);

            assertThat(result).isNotNull();
            assertThat(testUser.getRoles()).doesNotContain(new Role(RoleName.ROLE_ADMIN));
        }
    }

    // Enable/disable tests

    @Nested
    @DisplayName("Enable/Disable Tests")
    class EnableDisableTests {

        @Test
        @DisplayName("Should enable user")
        void shouldEnableUser() {
            testUser.setEnabled(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            userService.enableUser(1L);

            assertThat(testUser.isEnabled()).isTrue();
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should disable user")
        void shouldDisableUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            userService.disableUser(1L);

            assertThat(testUser.isEnabled()).isFalse();
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found for enable")
        void shouldThrowException_whenUserNotFoundForEnable() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.enableUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when user not found for disable")
        void shouldThrowException_whenUserNotFoundForDisable() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.disableUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Address tests

    @Nested
    @DisplayName("Address Management Tests")
    class AddressManagementTests {

        @Test
        @DisplayName("Should add address to user")
        void shouldAddAddressToUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(addAddressRequest)).thenReturn(testAddress);
            when(addressRepository.countByUserId(1L)).thenReturn(0L);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(Address.class))).thenReturn(testAddressResponse);

            AddressResponse result = userService.addAddress(1L, addAddressRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        @DisplayName("Should set first address as default")
        void shouldSetFirstAddressAsDefault() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(addressMapper.toEntity(addAddressRequest)).thenReturn(testAddress);
            when(addressRepository.countByUserId(1L)).thenReturn(0L);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(addressMapper.toResponse(any(Address.class))).thenReturn(testAddressResponse);

            userService.addAddress(1L, addAddressRequest);

            assertThat(testAddress.getDefaultAddress()).isTrue();
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found for address")
        void shouldThrowException_whenUserNotFoundForAddress() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.addAddress(999L, addAddressRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should set default address")
        void shouldSetDefaultAddress() {
            when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

            userService.setDefaultAddress(1L, 1L);

            assertThat(testAddress.getDefaultAddress()).isTrue();
            verify(addressRepository).clearDefaultAddressFlag(1L);
        }

        @Test
        @DisplayName("Should throw exception when address not found for default")
        void shouldThrowException_whenAddressNotFoundForDefault() {
            when(addressRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.setDefaultAddress(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when address does not belong to user")
        void shouldThrowException_whenAddressNotBelongToUser() {
            User otherUser = UserTestHelper.defaultUser(2L);
            Address otherAddress = AddressTestHelper.defaultAddress(2L, otherUser);

            when(addressRepository.findById(1L)).thenReturn(Optional.of(otherAddress));

            assertThatThrownBy(() -> userService.setDefaultAddress(1L, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Address does not belong to the user");
        }

        @Test
        @DisplayName("Should delete address")
        void shouldDeleteAddress() {
            when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

            userService.deleteAddress(1L, 1L);

            verify(addressRepository).delete(testAddress);
        }

        @Test
        @DisplayName("Should throw exception when address not found for delete")
        void shouldThrowException_whenAddressNotFoundForDelete() {
            when(addressRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteAddress(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when address does not belong to user for delete")
        void shouldThrowException_whenAddressNotBelongToUserForDelete() {
            User otherUser = UserTestHelper.defaultUser(2L);
            Address otherAddress = AddressTestHelper.defaultAddress(2L, otherUser);

            when(addressRepository.findById(1L)).thenReturn(Optional.of(otherAddress));

            assertThatThrownBy(() -> userService.deleteAddress(1L, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Address does not belong to the user");
        }
    }

    // Query tests

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnUserById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowException_whenUserNotFoundById() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return user by email")
        void shouldReturnUserByEmail() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            UserResponse result = userService.findByEmail("test@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found by email")
        void shouldThrowException_whenUserNotFoundByEmail() {
            when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("notfound@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            when(userMapper.toResponse(any(User.class))).thenReturn(testResponse);

            List<UserResponse> results = userService.findAll();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return user addresses")
        void shouldReturnUserAddresses() {
            when(addressRepository.findByUserId(1L)).thenReturn(List.of(testAddress));
            when(addressMapper.toResponseList(anyList())).thenReturn(List.of(testAddressResponse));

            List<AddressResponse> results = userService.getUserAddresses(1L);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return default address")
        void shouldReturnDefaultAddress() {
            when(addressRepository.findByUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.of(testAddress));
            when(addressMapper.toResponse(any(Address.class))).thenReturn(testAddressResponse);

            AddressResponse result = userService.getDefaultAddress(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when no default address")
        void shouldThrowException_whenNoDefaultAddress() {
            when(addressRepository.findByUserIdAndDefaultAddressTrue(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getDefaultAddress(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}