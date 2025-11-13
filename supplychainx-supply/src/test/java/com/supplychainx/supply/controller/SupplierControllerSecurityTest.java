package com.supplychainx.supply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.service.SupplierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de sécurité pour le SupplierController
 * Vérifie que les permissions sont correctement appliquées
 */
@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SupplierController - Tests de Sécurité")
class SupplierControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierService supplierService;

    // ==================================================================================
    //                          TESTS - GET ALL SUPPLIERS
    // ==================================================================================

    @Nested
    @DisplayName("GET /api/supply/suppliers - Récupérer tous les fournisseurs")
    class GetAllSuppliersTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_READ → 200 OK")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_READ"})
        void testGetAllSuppliers_WithPermission_ShouldSucceed() throws Exception {
            // Arrange
            List<SupplierResponseDTO> suppliers = Arrays.asList(
                createSupplierResponse(1L, "Supplier A"),
                createSupplierResponse(2L, "Supplier B")
            );
            PageResponse<SupplierResponseDTO> pageResponse = PageResponse.of(suppliers, 0, 10, 2, 1);
            when(supplierService.findAll(any(Pageable.class))).thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/supply/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Supplier A"));
        }

        @Test
        @DisplayName("❌ Sans permission → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_PRODUCT_READ"})
        void testGetAllSuppliers_WithoutPermission_ShouldFail() throws Exception {
            mockMvc.perform(get("/api/supply/suppliers"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Utilisateur anonyme → 401 Unauthorized")
        @WithAnonymousUser
        void testGetAllSuppliers_Anonymous_ShouldFail() throws Exception {
            mockMvc.perform(get("/api/supply/suppliers"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ==================================================================================
    //                          TESTS - GET SUPPLIER BY ID
    // ==================================================================================

    @Nested
    @DisplayName("GET /api/supply/suppliers/{id} - Récupérer un fournisseur")
    class GetSupplierByIdTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_READ → 200 OK")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_READ"})
        void testGetSupplierById_WithPermission_ShouldSucceed() throws Exception {
            // Arrange
            SupplierResponseDTO supplier = createSupplierResponse(1L, "Supplier A");
            when(supplierService.findById(1L)).thenReturn(supplier);

            // Act & Assert
            mockMvc.perform(get("/api/supply/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier A"));
        }

        @Test
        @DisplayName("❌ Sans permission → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_PRODUCT_READ"})
        void testGetSupplierById_WithoutPermission_ShouldFail() throws Exception {
            mockMvc.perform(get("/api/supply/suppliers/1"))
                .andExpect(status().isForbidden());
        }
    }

    // ==================================================================================
    //                          TESTS - CREATE SUPPLIER
    // ==================================================================================

    @Nested
    @DisplayName("POST /api/supply/suppliers - Créer un fournisseur")
    class CreateSupplierTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_CREATE → 201 Created")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_CREATE"})
        void testCreateSupplier_WithPermission_ShouldSucceed() throws Exception {
            // Arrange
            SupplierRequestDTO request = createSupplierRequest("New Supplier");
            SupplierResponseDTO response = createSupplierResponse(3L, "New Supplier");
            when(supplierService.create(any(SupplierRequestDTO.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/supply/suppliers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Supplier"));
        }

        @Test
        @DisplayName("❌ Avec seulement PERM_SUPPLIER_READ → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_READ"})
        void testCreateSupplier_WithOnlyReadPermission_ShouldFail() throws Exception {
            SupplierRequestDTO request = createSupplierRequest("New Supplier");

            mockMvc.perform(post("/api/supply/suppliers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ Sans permission → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_PRODUCT_CREATE"})
        void testCreateSupplier_WithoutPermission_ShouldFail() throws Exception {
            SupplierRequestDTO request = createSupplierRequest("New Supplier");

            mockMvc.perform(post("/api/supply/suppliers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }
    }

    // ==================================================================================
    //                          TESTS - UPDATE SUPPLIER
    // ==================================================================================

    @Nested
    @DisplayName("PUT /api/supply/suppliers/{id} - Mettre à jour un fournisseur")
    class UpdateSupplierTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_UPDATE → 200 OK")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_UPDATE"})
        void testUpdateSupplier_WithPermission_ShouldSucceed() throws Exception {
            // Arrange
            SupplierRequestDTO request = createSupplierRequest("Updated Supplier");
            SupplierResponseDTO response = createSupplierResponse(1L, "Updated Supplier");
            when(supplierService.update(anyLong(), any(SupplierRequestDTO.class)))
                .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/supply/suppliers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Supplier"));
        }

        @Test
        @DisplayName("❌ Sans permission → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_READ", "PERM_SUPPLIER_CREATE"})
        void testUpdateSupplier_WithoutPermission_ShouldFail() throws Exception {
            SupplierRequestDTO request = createSupplierRequest("Updated Supplier");

            mockMvc.perform(put("/api/supply/suppliers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }
    }

    // ==================================================================================
    //                          TESTS - DELETE SUPPLIER
    // ==================================================================================

    @Nested
    @DisplayName("DELETE /api/supply/suppliers/{id} - Supprimer un fournisseur")
    class DeleteSupplierTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_DELETE → 204 No Content")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_DELETE"})
        void testDeleteSupplier_WithPermission_ShouldSucceed() throws Exception {
            mockMvc.perform(delete("/api/supply/suppliers/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("❌ Sans permission → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_READ", "PERM_SUPPLIER_CREATE", "PERM_SUPPLIER_UPDATE"})
        void testDeleteSupplier_WithoutPermission_ShouldFail() throws Exception {
            mockMvc.perform(delete("/api/supply/suppliers/1"))
                .andExpect(status().isForbidden());
        }
    }

    // ==================================================================================
    //                          TESTS - MULTIPLE PERMISSIONS (hasAnyPermission)
    // ==================================================================================

    @Nested
    @DisplayName("Tests avec hasAnyPermission")
    class HasAnyPermissionTests {

        @Test
        @DisplayName("✅ Avec PERM_SUPPLIER_UPDATE (parmi READ/UPDATE/DELETE) → 200 OK")
        @WithMockUser(username = "user", authorities = {"PERM_SUPPLIER_UPDATE"})
        void testWithAnyPermission_HasOne_ShouldSucceed() throws Exception {
            // Si un endpoint utilise hasAnyPermission('SUPPLIER_READ', 'SUPPLIER_UPDATE', 'SUPPLIER_DELETE')
            SupplierResponseDTO supplier = createSupplierResponse(1L, "Supplier A");
            when(supplierService.findById(1L)).thenReturn(supplier);

            mockMvc.perform(get("/api/supply/suppliers/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("❌ Sans aucune des permissions requises → 403 Forbidden")
        @WithMockUser(username = "user", authorities = {"PERM_PRODUCT_READ"})
        void testWithAnyPermission_HasNone_ShouldFail() throws Exception {
            mockMvc.perform(get("/api/supply/suppliers/1"))
                .andExpect(status().isForbidden());
        }
    }

    // ==================================================================================
    //                          TESTS - ROLE-BASED (ADMIN)
    // ==================================================================================

    @Nested
    @DisplayName("Tests avec rôle ADMIN")
    class AdminRoleTests {

        @Test
        @DisplayName("✅ Avec ROLE_ADMIN → Accès à tout")
        @WithMockUser(username = "admin", roles = {"ADMIN"}, authorities = {"ROLE_ADMIN", "PERM_SUPPLIER_READ", "PERM_SUPPLIER_CREATE", "PERM_SUPPLIER_UPDATE", "PERM_SUPPLIER_DELETE"})
        void testAdmin_HasAccessToAll() throws Exception {
            // GET
            PageResponse<SupplierResponseDTO> pageResponse = PageResponse.of(Arrays.asList(), 0, 10, 0, 0);
            when(supplierService.findAll(any(Pageable.class))).thenReturn(pageResponse);
            
            mockMvc.perform(get("/api/supply/suppliers"))
                .andExpect(status().isOk());

            // POST
            SupplierRequestDTO request = createSupplierRequest("Admin Supplier");
            SupplierResponseDTO response = createSupplierResponse(99L, "Admin Supplier");
            when(supplierService.create(any())).thenReturn(response);

            mockMvc.perform(post("/api/supply/suppliers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            // DELETE
            mockMvc.perform(delete("/api/supply/suppliers/1"))
                .andExpect(status().isNoContent());
        }
    }

    // ==================================================================================
    //                              HELPER METHODS
    // ==================================================================================

    private SupplierResponseDTO createSupplierResponse(Long id, String name) {
        return SupplierResponseDTO.builder()
            .id(id)
            .name(name)
            .email("contact@" + name.toLowerCase().replace(" ", "") + ".com")
            .phone("+1234567890")
            .address("123 Main St")
            .build();
    }

    private SupplierRequestDTO createSupplierRequest(String name) {
        return SupplierRequestDTO.builder()
            .name(name)
            .email("contact@" + name.toLowerCase().replace(" ", "") + ".com")
            .phone("+1234567890")
            .address("123 Main St")
            .build();
    }
}
