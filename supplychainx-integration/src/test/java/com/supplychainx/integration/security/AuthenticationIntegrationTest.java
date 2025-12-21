package com.supplychainx.integration.security;

import com.supplychainx.integration.config.IntegrationTest;
import com.supplychainx.security.repository.RefreshTokenRepository;
import com.supplychainx.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("Integration Tests - Authentication & Registration")
class AuthenticationIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up refresh tokens before each test to ensure isolation
        refreshTokenRepository.deleteAll();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should authenticate with valid credentials and return JWT + refresh token")
    void shouldAuthenticateWithValidCredentials() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        long tokenCountBefore = refreshTokenRepository.count();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));

        // Verify refresh token is stored in database
        long tokenCountAfter = refreshTokenRepository.count();
        assertThat(tokenCountAfter).isEqualTo(tokenCountBefore + 1);
    }

    @Test
    @DisplayName("Should return 401 with invalid credentials")
    void shouldReturn401WithInvalidCredentials() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "wrongpassword"
                }
                """;

        long tokenCountBefore = refreshTokenRepository.count();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        // Verify no refresh token was created
        long tokenCountAfter = refreshTokenRepository.count();
        assertThat(tokenCountAfter).isEqualTo(tokenCountBefore);
    }

    @Test
    @DisplayName("Should return 400 with missing username")
    void shouldReturn400WithMissingUsername() throws Exception {
        // Given
        String loginRequest = """
                {
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 with missing password")
    void shouldReturn400WithMissingPassword() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should authenticate supply manager and return appropriate token")
    void shouldAuthenticateSupplyManager() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("supply_manager"))
                .andExpect(jsonPath("$.user.role").value("GESTIONNAIRE_APPROVISIONNEMENT"));
    }

    @Test
    @DisplayName("Should not authenticate disabled user")
    void shouldNotAuthenticateDisabledUser() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "user_disabled",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should create multiple refresh tokens for multiple logins")
    void shouldCreateMultipleRefreshTokensForMultipleLogins() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        long tokenCountBefore = refreshTokenRepository.count();

        // When - Login 3 times (simulating different devices)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk());

        // Then - Verify 3 new refresh tokens were created
        long tokenCountAfter = refreshTokenRepository.count();
        assertThat(tokenCountAfter).isEqualTo(tokenCountBefore + 3);
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should register new user and return JWT + refresh token")
    void shouldRegisterNewUser() throws Exception {
        // Given
        long userCountBefore = userRepository.count();
        long tokenCountBefore = refreshTokenRepository.count();

        String registerRequest = """
                {
                    "username": "newuser",
                    "password": "SecurePass123!",
                    "email": "newuser@supplychainx.com",
                    "firstName": "New",
                    "lastName": "User",
                    "role": "GESTIONNAIRE_APPROVISIONNEMENT"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.email").value("newuser@supplychainx.com"))
                .andExpect(jsonPath("$.user.role").value("GESTIONNAIRE_APPROVISIONNEMENT"));

        // Verify user was created
        long userCountAfter = userRepository.count();
        assertThat(userCountAfter).isEqualTo(userCountBefore + 1);

        // Verify refresh token was created
        long tokenCountAfter = refreshTokenRepository.count();
        assertThat(tokenCountAfter).isEqualTo(tokenCountBefore + 1);
    }

    @Test
    @DisplayName("Should reject registration with weak password")
    void shouldRejectWeakPassword() throws Exception {
        // Given
        String registerRequest = """
                {
                    "username": "weakuser",
                    "password": "weak",
                    "email": "weak@supplychainx.com",
                    "firstName": "Weak",
                    "lastName": "User",
                    "role": "GESTIONNAIRE_APPROVISIONNEMENT"
                }
                """;

        long userCountBefore = userRepository.count();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Verify user was not created
        long userCountAfter = userRepository.count();
        assertThat(userCountAfter).isEqualTo(userCountBefore);
    }

    @Test
    @DisplayName("Should reject registration with duplicate username")
    void shouldRejectDuplicateUsername() throws Exception {
        // Given - admin already exists
        String registerRequest = """
                {
                    "username": "admin",
                    "password": "SecurePass123!",
                    "email": "admin2@supplychainx.com",
                    "firstName": "Admin",
                    "lastName": "Two",
                    "role": "ADMIN"
                }
                """;

        long userCountBefore = userRepository.count();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Verify user was not created
        long userCountAfter = userRepository.count();
        assertThat(userCountAfter).isEqualTo(userCountBefore);
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void shouldRejectInvalidEmail() throws Exception {
        // Given
        String registerRequest = """
                {
                    "username": "emailtest",
                    "password": "SecurePass123!",
                    "email": "invalid-email",
                    "firstName": "Email",
                    "lastName": "Test",
                    "role": "GESTIONNAIRE_APPROVISIONNEMENT"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with missing required fields")
    void shouldRejectMissingRequiredFields() throws Exception {
        // Given - missing firstName and lastName
        String registerRequest = """
                {
                    "username": "incomplete",
                    "password": "SecurePass123!",
                    "email": "incomplete@supplychainx.com",
                    "role": "GESTIONNAIRE_APPROVISIONNEMENT"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully login after registration")
    void shouldLoginAfterRegistration() throws Exception {
        // Given - Register a new user
        String registerRequest = """
                {
                    "username": "logintest",
                    "password": "SecurePass123!",
                    "email": "logintest@supplychainx.com",
                    "firstName": "Login",
                    "lastName": "Test",
                    "role": "GESTIONNAIRE_APPROVISIONNEMENT"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andReturn();

        // When - Login with the same credentials
        String loginRequest = """
                {
                    "username": "logintest",
                    "password": "SecurePass123!"
                }
                """;

        // Then - Login should succeed
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("logintest"));

        // Verify 2 refresh tokens exist (one from register, one from login)
        long tokenCount = refreshTokenRepository.count();
        assertThat(tokenCount).isEqualTo(2);
    }

    // ==================== JWT TOKEN FORMAT TESTS ====================

    @Test
    @DisplayName("JWT token should follow proper format (header.payload.signature)")
    void jwtTokenShouldFollowProperFormat() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")))
                .andReturn();
    }

    @Test
    @DisplayName("Refresh token should be UUID format")
    void refreshTokenShouldBeUUIDFormat() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        // When & Then - UUID pattern: 8-4-4-4-12 hex digits
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value(matchesPattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")));
    }
}
