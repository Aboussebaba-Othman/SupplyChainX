package com.supplychainx.integration.security;

import com.supplychainx.integration.config.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour l'authentification JWT
 */
@DisplayName("Integration Tests - Authentication")
class AuthenticationIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should authenticate with valid credentials and return JWT token")
    void shouldAuthenticateWithValidCredentials() throws Exception {
        // Given
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
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

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication Failed"));
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
                .andExpect(jsonPath("$.username").value("supply_manager"))
                .andExpect(jsonPath("$.role").value("GESTIONNAIRE_APPROVISIONNEMENT"));
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
                .andExpect(jsonPath("$.error").value("Authentication Failed"));
    }
}
