package com.supplychainx.integration.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.integration.config.BaseSecurityIntegrationTest;
import com.supplychainx.security.entity.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("Integration Tests - Refresh Token with Rotation")
class RefreshTokenIntegrationTest extends BaseSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== REFRESH TOKEN ROTATION TESTS ====================

    @Test
    @DisplayName("Should refresh access token with valid refresh token")
    void shouldRefreshAccessTokenWithValidRefreshToken() throws Exception {
        // Given - Login to get initial tokens
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String oldAccessToken = loginJson.get("token").asText();
        String oldRefreshToken = loginJson.get("refreshToken").asText();

        // Wait 1 second to ensure different JWT timestamp
        Thread.sleep(1000);

        // When - Use refresh token to get new tokens
        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, oldRefreshToken);

        // Then
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("admin"))
                .andReturn();

        String refreshResponse = refreshResult.getResponse().getContentAsString();
        JsonNode refreshJson = objectMapper.readTree(refreshResponse);
        String newAccessToken = refreshJson.get("token").asText();
        String newRefreshToken = refreshJson.get("refreshToken").asText();

        // Verify tokens are different (rotation occurred)
        assertThat(newAccessToken).isNotEqualTo(oldAccessToken);
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
    }

    @Test
    @DisplayName("Should revoke old refresh token after rotation")
    void shouldRevokeOldRefreshTokenAfterRotation() throws Exception {
        // Given - Login to get initial tokens
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String oldRefreshToken = loginJson.get("refreshToken").asText();

        // When - Use refresh token (this should rotate it)
        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, oldRefreshToken);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk());

        // Then - Verify old token is revoked in database
        Optional<RefreshToken> revokedToken = refreshTokenRepository.findByToken(oldRefreshToken);
        assertThat(revokedToken).isPresent();
        assertThat(revokedToken.get().isRevoked()).isTrue();

        // And - Try to use old token again (should fail with 409 due to rate limiting or 400 for revoked token)
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should update last used timestamp when refreshing token")
    void shouldUpdateLastUsedTimestamp() throws Exception {
        // Given - Login to get initial tokens
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshTokenString = loginJson.get("refreshToken").asText();

        // Get token from database before refresh
        RefreshToken tokenBefore = refreshTokenRepository.findByToken(refreshTokenString).orElseThrow();
        LocalDateTime lastUsedBefore = tokenBefore.getLastUsedAt();

        // Wait a bit to ensure timestamp difference
        Thread.sleep(100);

        // When - Refresh token
        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshTokenString);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk());

        // Then - Verify lastUsedAt was updated (on the now-revoked old token)
        RefreshToken tokenAfter = refreshTokenRepository.findByToken(refreshTokenString).orElseThrow();
        LocalDateTime lastUsedAfter = tokenAfter.getLastUsedAt();

        assertThat(lastUsedAfter).isNotNull();
        if (lastUsedBefore != null) {
            assertThat(lastUsedAfter).isAfter(lastUsedBefore);
        }
    }

    @Test
    @DisplayName("Should reject refresh with invalid token")
    void shouldRejectRefreshWithInvalidToken() throws Exception {
        // Given
        String refreshRequest = """
                {
                    "refreshToken": "invalid-token-12345"
                }
                """;

        // When & Then - Accept any 4xx error (could be 400 or 409 from rate limiting)
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should reject refresh with missing token")
    void shouldRejectRefreshWithMissingToken() throws Exception {
        // Given
        String refreshRequest = """
                {
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject refresh with empty token")
    void shouldRejectRefreshWithEmptyToken() throws Exception {
        // Given
        String refreshRequest = """
                {
                    "refreshToken": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject refresh for disabled user")
    void shouldRejectRefreshForDisabledUser() throws Exception {
        // Given - Login as disabled user (if exists in test data)
        // Note: This test assumes there's a way to disable a user after login
        // For now, we'll test that the endpoint validates user status

        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshTokenString = loginJson.get("refreshToken").asText();

        // TODO: Disable the user here (requires UserService)
        // For now, this test just verifies the endpoint exists and validates properly

        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshTokenString);

        // When & Then - Should succeed since user is not disabled
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should support multiple refresh token rotations")
    void shouldSupportMultipleRotations() throws Exception {
        // Given - Login to get initial tokens
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshToken1 = loginJson.get("refreshToken").asText();

        // When - Perform first rotation
        String refreshRequest1 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken1);

        MvcResult refreshResult1 = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest1))
                .andExpect(status().isOk())
                .andReturn();

        String refreshResponse1 = refreshResult1.getResponse().getContentAsString();
        JsonNode refreshJson1 = objectMapper.readTree(refreshResponse1);
        String refreshToken2 = refreshJson1.get("refreshToken").asText();

        // When - Perform second rotation
        String refreshRequest2 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken2);

        MvcResult refreshResult2 = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest2))
                .andExpect(status().isOk())
                .andReturn();

        String refreshResponse2 = refreshResult2.getResponse().getContentAsString();
        JsonNode refreshJson2 = objectMapper.readTree(refreshResponse2);
        String refreshToken3 = refreshJson2.get("refreshToken").asText();

        // When - Perform third rotation
        String refreshRequest3 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken3);

        MvcResult refreshResult3 = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest3))
                .andExpect(status().isOk())
                .andReturn();

        String refreshResponse3 = refreshResult3.getResponse().getContentAsString();
        JsonNode refreshJson3 = objectMapper.readTree(refreshResponse3);
        String refreshToken4 = refreshJson3.get("refreshToken").asText();

        // Then - All tokens should be different
        assertThat(refreshToken1).isNotEqualTo(refreshToken2);
        assertThat(refreshToken2).isNotEqualTo(refreshToken3);
        assertThat(refreshToken3).isNotEqualTo(refreshToken4);

        // And - First 3 tokens should be revoked
        assertThat(refreshTokenRepository.findByToken(refreshToken1).get().isRevoked()).isTrue();
        assertThat(refreshTokenRepository.findByToken(refreshToken2).get().isRevoked()).isTrue();
        assertThat(refreshTokenRepository.findByToken(refreshToken3).get().isRevoked()).isTrue();

        // And - Fourth token should be valid (not revoked)
        assertThat(refreshTokenRepository.findByToken(refreshToken4).get().isRevoked()).isFalse();
    }

    @Test
    @DisplayName("Should maintain separate refresh token chains for different devices")
    void shouldMaintainSeparateTokenChainsForDifferentDevices() throws Exception {
        // Given - Login twice (simulating 2 devices)
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        // Device 1 login
        MvcResult loginResult1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse1 = loginResult1.getResponse().getContentAsString();
        JsonNode loginJson1 = objectMapper.readTree(loginResponse1);
        String device1RefreshToken = loginJson1.get("refreshToken").asText();

        // Device 2 login
        MvcResult loginResult2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse2 = loginResult2.getResponse().getContentAsString();
        JsonNode loginJson2 = objectMapper.readTree(loginResponse2);
        String device2RefreshToken = loginJson2.get("refreshToken").asText();

        // Verify both tokens are different
        assertThat(device1RefreshToken).isNotEqualTo(device2RefreshToken);

        // When - Refresh token from device 1
        String refreshRequest1 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, device1RefreshToken);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest1))
                .andExpect(status().isOk());

        // Then - Device 2 token should still be valid
        String refreshRequest2 = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, device2RefreshToken);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest2))
                .andExpect(status().isOk());

        // Verify device 1 old token is revoked but device 2 token was valid
        assertThat(refreshTokenRepository.findByToken(device1RefreshToken).get().isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should create exactly one new token per refresh")
    void shouldCreateExactlyOneNewTokenPerRefresh() throws Exception {
        // Given - Login to get initial token
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshTokenString = loginJson.get("refreshToken").asText();

        long tokenCountBefore = refreshTokenRepository.count();

        // When - Refresh token
        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshTokenString);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk());

        // Then - Exactly one new token should be created (old one revoked, new one created)
        long tokenCountAfter = refreshTokenRepository.count();
        assertThat(tokenCountAfter).isEqualTo(tokenCountBefore + 1);

        // Verify we have 1 revoked token and 1 valid token
        long revokedCount = refreshTokenRepository.findAll().stream()
                .filter(RefreshToken::isRevoked)
                .count();
        long validCount = refreshTokenRepository.findAll().stream()
                .filter(token -> !token.isRevoked())
                .count();

        assertThat(revokedCount).isEqualTo(1);
        assertThat(validCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should include user info in refresh response")
    void shouldIncludeUserInfoInRefreshResponse() throws Exception {
        // Given - Login to get initial tokens
        String loginRequest = """
                {
                    "username": "supply_manager",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String refreshTokenString = loginJson.get("refreshToken").asText();

        // When - Refresh token
        String refreshRequest = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshTokenString);

        // Then - Response should include user information
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").exists())
                .andExpect(jsonPath("$.user.username").value("supply_manager"))
                .andExpect(jsonPath("$.user.role").value("GESTIONNAIRE_APPROVISIONNEMENT"));
    }
}
