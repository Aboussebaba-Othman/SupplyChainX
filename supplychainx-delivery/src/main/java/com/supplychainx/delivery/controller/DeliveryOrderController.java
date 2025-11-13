package com.supplychainx.delivery.controller;

import com.supplychainx.delivery.dto.request.DeliveryOrderRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryOrderResponseDTO;
import com.supplychainx.delivery.enums.OrderStatus;
import com.supplychainx.delivery.service.DeliveryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/delivery/orders")
@RequiredArgsConstructor
@Slf4j
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;

    /**
     * Créer une nouvelle commande
     * POST /api/delivery/orders
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_CREATE')")
    @PostMapping
    public ResponseEntity<DeliveryOrderResponseDTO> create(@Valid @RequestBody DeliveryOrderRequestDTO requestDTO) {
        log.info("REST request to create DeliveryOrder : {}", requestDTO.getOrderNumber());
        DeliveryOrderResponseDTO response = deliveryOrderService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mettre à jour une commande existante
     * PUT /api/delivery/orders/{id}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryOrderResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryOrderRequestDTO requestDTO) {
        log.info("REST request to update DeliveryOrder : {}", id);
        DeliveryOrderResponseDTO response = deliveryOrderService.update(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une commande par ID
     * GET /api/delivery/orders/{id}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryOrderResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get DeliveryOrder : {}", id);
        DeliveryOrderResponseDTO response = deliveryOrderService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une commande par numéro
     * GET /api/delivery/orders/number/{orderNumber}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<DeliveryOrderResponseDTO> getByOrderNumber(@PathVariable String orderNumber) {
        log.info("REST request to get DeliveryOrder by number : {}", orderNumber);
        DeliveryOrderResponseDTO response = deliveryOrderService.getByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les commandes avec pagination
     * GET /api/delivery/orders
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping
    public ResponseEntity<Page<DeliveryOrderResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all DeliveryOrders - page: {}", pageable.getPageNumber());
        Page<DeliveryOrderResponseDTO> response = deliveryOrderService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les commandes par statut
     * GET /api/delivery/orders/status/{status}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DeliveryOrderResponseDTO>> getByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get DeliveryOrders by status : {}", status);
        Page<DeliveryOrderResponseDTO> response = deliveryOrderService.getByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les commandes d'un client
     * GET /api/delivery/orders/customer/{customerId}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<DeliveryOrderResponseDTO>> getByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get DeliveryOrders by customer : {}", customerId);
        Page<DeliveryOrderResponseDTO> response = deliveryOrderService.getByCustomer(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les commandes entre deux dates
     * GET /api/delivery/orders/date-range?startDate=2024-01-01&endDate=2024-12-31
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/date-range")
    public ResponseEntity<Page<DeliveryOrderResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get DeliveryOrders between {} and {}", startDate, endDate);
        Page<DeliveryOrderResponseDTO> response = deliveryOrderService.getByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les commandes en retard
     * GET /api/delivery/orders/delayed
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_READ')")
    @GetMapping("/delayed")
    public ResponseEntity<List<DeliveryOrderResponseDTO>> getDelayedOrders() {
        log.info("REST request to get delayed DeliveryOrders");
        List<DeliveryOrderResponseDTO> response = deliveryOrderService.getDelayedOrders();
        return ResponseEntity.ok(response);
    }

    /**
     * Changer le statut d'une commande
     * PATCH /api/delivery/orders/{id}/status
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_VALIDATE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.info("REST request to update DeliveryOrder status : {} to {}", id, status);
        DeliveryOrderResponseDTO response = deliveryOrderService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une commande
     * DELETE /api/delivery/orders/{id}
     */
    @PreAuthorize("@securityExpressions.hasPermission('DELIVERY_ORDER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete DeliveryOrder : {}", id);
        deliveryOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
