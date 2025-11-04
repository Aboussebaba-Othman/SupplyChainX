package com.supplychainx.delivery.controller;

import com.supplychainx.delivery.dto.request.DeliveryRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryResponseDTO;
import com.supplychainx.delivery.enums.DeliveryStatus;
import com.supplychainx.delivery.service.DeliveryService;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/delivery/deliveries")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * Créer une nouvelle livraison
     * POST /api/delivery/deliveries
     */
    @PostMapping
    public ResponseEntity<DeliveryResponseDTO> create(@Valid @RequestBody DeliveryRequestDTO requestDTO) {
        log.info("REST request to create Delivery : {}", requestDTO.getDeliveryNumber());
        DeliveryResponseDTO response = deliveryService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mettre à jour une livraison existante
     * PUT /api/delivery/deliveries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryRequestDTO requestDTO) {
        log.info("REST request to update Delivery : {}", id);
        DeliveryResponseDTO response = deliveryService.update(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une livraison par ID
     * GET /api/delivery/deliveries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponseDTO> getById(@PathVariable Long id) {
        log.info("REST request to get Delivery : {}", id);
        DeliveryResponseDTO response = deliveryService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une livraison par numéro
     * GET /api/delivery/deliveries/number/{deliveryNumber}
     */
    @GetMapping("/number/{deliveryNumber}")
    public ResponseEntity<DeliveryResponseDTO> getByDeliveryNumber(@PathVariable String deliveryNumber) {
        log.info("REST request to get Delivery by number : {}", deliveryNumber);
        DeliveryResponseDTO response = deliveryService.getByDeliveryNumber(deliveryNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une livraison par numéro de suivi
     * GET /api/delivery/deliveries/tracking/{trackingNumber}
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<DeliveryResponseDTO> getByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("REST request to get Delivery by tracking number : {}", trackingNumber);
        DeliveryResponseDTO response = deliveryService.getByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer la livraison d'une commande
     * GET /api/delivery/deliveries/order/{deliveryOrderId}
     */
    @GetMapping("/order/{deliveryOrderId}")
    public ResponseEntity<DeliveryResponseDTO> getByDeliveryOrder(@PathVariable Long deliveryOrderId) {
        log.info("REST request to get Delivery by order : {}", deliveryOrderId);
        DeliveryResponseDTO response = deliveryService.getByDeliveryOrder(deliveryOrderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les livraisons avec pagination
     * GET /api/delivery/deliveries
     */
    @GetMapping
    public ResponseEntity<Page<DeliveryResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "deliveryDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get all Deliveries - page: {}", pageable.getPageNumber());
        Page<DeliveryResponseDTO> response = deliveryService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les livraisons par statut
     * GET /api/delivery/deliveries/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<DeliveryResponseDTO>> getByStatus(
            @PathVariable DeliveryStatus status,
            @PageableDefault(size = 20, sort = "deliveryDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get Deliveries by status : {}", status);
        Page<DeliveryResponseDTO> response = deliveryService.getByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les livraisons par date
     * GET /api/delivery/deliveries/date/{date}
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<DeliveryResponseDTO>> getByDeliveryDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request to get Deliveries by date : {}", date);
        List<DeliveryResponseDTO> response = deliveryService.getByDeliveryDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les livraisons en retard
     * GET /api/delivery/deliveries/delayed
     */
    @GetMapping("/delayed")
    public ResponseEntity<List<DeliveryResponseDTO>> getDelayedDeliveries() {
        log.info("REST request to get delayed Deliveries");
        List<DeliveryResponseDTO> response = deliveryService.getDelayedDeliveries();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les livraisons actives d'un chauffeur
     * GET /api/delivery/deliveries/driver/{driver}
     */
    @GetMapping("/driver/{driver}")
    public ResponseEntity<List<DeliveryResponseDTO>> getActiveDeliveriesByDriver(@PathVariable String driver) {
        log.info("REST request to get active Deliveries by driver : {}", driver);
        List<DeliveryResponseDTO> response = deliveryService.getActiveDeliveriesByDriver(driver);
        return ResponseEntity.ok(response);
    }

    /**
     * Changer le statut d'une livraison
     * PATCH /api/delivery/deliveries/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {
        log.info("REST request to update Delivery status : {} to {}", id, status);
        DeliveryResponseDTO response = deliveryService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer une livraison comme livrée
     * PATCH /api/delivery/deliveries/{id}/deliver
     */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<DeliveryResponseDTO> markAsDelivered(@PathVariable Long id) {
        log.info("REST request to mark Delivery as delivered : {}", id);
        DeliveryResponseDTO response = deliveryService.markAsDelivered(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une livraison
     * DELETE /api/delivery/deliveries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete Delivery : {}", id);
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
