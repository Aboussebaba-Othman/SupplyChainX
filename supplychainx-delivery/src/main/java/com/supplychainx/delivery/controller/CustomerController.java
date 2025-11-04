package com.supplychainx.delivery.controller;

import com.supplychainx.delivery.dto.request.CustomerRequestDTO;
import com.supplychainx.delivery.dto.response.CustomerResponseDTO;
import com.supplychainx.delivery.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Créer un nouveau client
     * POST /api/delivery/customers
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("REST request to create Customer : {}", requestDTO.getCode());
        CustomerResponseDTO response = customerService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mettre à jour un client existant
     * PUT /api/delivery/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("REST request to update Customer : {}", id);
        CustomerResponseDTO response = customerService.update(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un client par ID
     * GET /api/delivery/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get Customer : {}", id);
        CustomerResponseDTO response = customerService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un client par code
     * GET /api/delivery/customers/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CustomerResponseDTO> getByCode(@PathVariable String code) {
        log.info("REST request to get Customer by code : {}", code);
        CustomerResponseDTO response = customerService.getByCode(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer tous les clients avec pagination
     * GET /api/delivery/customers
     */
    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("REST request to get all Customers - page: {}", pageable.getPageNumber());
        Page<CustomerResponseDTO> response = customerService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher des clients par mot-clé
     * GET /api/delivery/customers/search?keyword=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponseDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("REST request to search Customers with keyword : {}", keyword);
        Page<CustomerResponseDTO> response = customerService.search(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les clients par ville
     * GET /api/delivery/customers/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<Page<CustomerResponseDTO>> getByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("REST request to get Customers by city : {}", city);
        Page<CustomerResponseDTO> response = customerService.getByCity(city, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les clients par pays
     * GET /api/delivery/customers/country/{country}
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<Page<CustomerResponseDTO>> getByCountry(
            @PathVariable String country,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("REST request to get Customers by country : {}", country);
        Page<CustomerResponseDTO> response = customerService.getByCountry(country, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un client
     * DELETE /api/delivery/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete Customer : {}", id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
