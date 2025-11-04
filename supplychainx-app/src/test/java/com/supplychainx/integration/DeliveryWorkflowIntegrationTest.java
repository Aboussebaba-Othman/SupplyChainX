package com.supplychainx.integration;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.delivery.dto.request.CustomerRequestDTO;
import com.supplychainx.delivery.dto.request.DeliveryOrderLineRequestDTO;
import com.supplychainx.delivery.dto.request.DeliveryOrderRequestDTO;
import com.supplychainx.delivery.dto.request.DeliveryRequestDTO;
import com.supplychainx.delivery.dto.response.CustomerResponseDTO;
import com.supplychainx.delivery.dto.response.DeliveryOrderResponseDTO;
import com.supplychainx.delivery.dto.response.DeliveryResponseDTO;
import com.supplychainx.delivery.enums.DeliveryStatus;
import com.supplychainx.delivery.enums.OrderStatus;
import com.supplychainx.delivery.service.CustomerService;
import com.supplychainx.delivery.service.DeliveryOrderService;
import com.supplychainx.delivery.service.DeliveryService;
import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for complete Delivery workflow:
 * 1. Create Customer
 * 2. Create Products with stock
 * 3. Create DeliveryOrder with multiple lines
 * 4. Verify stock validation
 * 5. Create Delivery entity
 * 6. Update order status through workflow
 * 7. Mark delivery as completed
 * 8. Verify cascading deletes
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeliveryWorkflowIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private DeliveryOrderService deliveryOrderService;

    @Autowired
    private DeliveryService deliveryService;

    private Long customerId;
    private Long product1Id;
    private Long product2Id;

    @BeforeEach
    void setupTestData() {
        // 1. Create a customer
        CustomerRequestDTO customerRequest = CustomerRequestDTO.builder()
                .code("CUST-TEST-001")
                .name("ACME Corp Test")
                .contact("Jane Smith")
                .phone("+212600111222")
                .email("jane@acme-test.com")
                .address("10 Test Avenue")
                .city("Casablanca")
                .postalCode("20000")
                .country("Morocco")
                .build();
        CustomerResponseDTO customer = customerService.create(customerRequest);
        customerId = customer.getId();

        // 2. Create products with sufficient stock
        ProductRequestDTO product1Request = ProductRequestDTO.builder()
                .code("PROD-DEL-TEST-001")
                .name("Test Product 1")
                .description("Product for delivery testing")
                .category("Electronics")
                .productionTime(60)
                .cost(50.0)
                .stock(100.0)
                .stockMin(10.0)
                .build();
        ProductResponseDTO product1 = productService.createProduct(product1Request);
        product1Id = product1.getId();

        ProductRequestDTO product2Request = ProductRequestDTO.builder()
                .code("PROD-DEL-TEST-002")
                .name("Test Product 2")
                .description("Another product for testing")
                .category("Accessories")
                .productionTime(30)
                .cost(25.0)
                .stock(50.0)
                .stockMin(5.0)
                .build();
        ProductResponseDTO product2 = productService.createProduct(product2Request);
        product2Id = product2.getId();
    }

    @Test
    void testCompleteDeliveryWorkflow_success() {
        // 3. Create DeliveryOrder with multiple product lines
        DeliveryOrderLineRequestDTO line1 = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(5)
                .unitPrice(60.0)
                .build();

        DeliveryOrderLineRequestDTO line2 = DeliveryOrderLineRequestDTO.builder()
                .productId(product2Id)
                .quantity(3)
                .unitPrice(30.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-TEST-001")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(3))
                .deliveryAddress("10 Test Avenue")
                .deliveryCity("Casablanca")
                .deliveryPostalCode("20000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(Arrays.asList(line1, line2))
                .build();

        DeliveryOrderResponseDTO order = deliveryOrderService.create(orderRequest);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals("ORD-TEST-001", order.getOrderNumber());
        assertEquals(OrderStatus.EN_PREPARATION, order.getStatus());
        assertEquals(2, order.getOrderLines().size());
        assertEquals(390.0, order.getTotalAmount(), 0.01); // (5*60) + (3*30) = 390

        // 4. Verify customer association
        assertEquals("ACME Corp Test", order.getCustomer().getName());
        assertEquals("jane@acme-test.com", order.getCustomer().getEmail());

        // 5. Create Delivery entity for the order
        DeliveryRequestDTO deliveryRequest = DeliveryRequestDTO.builder()
                .deliveryNumber("DEL-TEST-001")
                .deliveryOrderId(order.getId())
                .vehicle("Truck-TEST-12")
                .driver("Ahmed Benali")
                .driverPhone("+212600999888")
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(LocalDate.now().plusDays(3))
                .cost(75.0)
                .trackingNumber("TRK-TEST-001")
                .build();

        DeliveryResponseDTO delivery = deliveryService.create(deliveryRequest);

        assertNotNull(delivery);
        assertNotNull(delivery.getId());
        assertEquals("DEL-TEST-001", delivery.getDeliveryNumber());
        assertEquals(DeliveryStatus.PLANIFIEE, delivery.getStatus());
        assertEquals("Ahmed Benali", delivery.getDriver());
        assertEquals("TRK-TEST-001", delivery.getTrackingNumber());
        assertNotNull(delivery.getDeliveryOrder());
        assertEquals(order.getId(), delivery.getDeliveryOrder().getId());

        // 6. Update order status to EN_ROUTE
        DeliveryOrderResponseDTO updatedOrder = deliveryOrderService.updateStatus(order.getId(), OrderStatus.EN_ROUTE);
        assertEquals(OrderStatus.EN_ROUTE, updatedOrder.getStatus());

        // 7. Update delivery status to EN_COURS
        DeliveryResponseDTO updatedDelivery = deliveryService.updateStatus(delivery.getId(), DeliveryStatus.EN_COURS);
        assertEquals(DeliveryStatus.EN_COURS, updatedDelivery.getStatus());

        // 8. Mark delivery as delivered
        DeliveryResponseDTO completedDelivery = deliveryService.markAsDelivered(delivery.getId());
        assertEquals(DeliveryStatus.LIVREE, completedDelivery.getStatus());
        assertNotNull(completedDelivery.getActualDeliveryDate());

        // 9. Update order status to LIVREE
        DeliveryOrderResponseDTO finalOrder = deliveryOrderService.updateStatus(order.getId(), OrderStatus.LIVREE);
        assertEquals(OrderStatus.LIVREE, finalOrder.getStatus());
        assertNotNull(finalOrder.getActualDeliveryDate());
    }

    @Test
    void testDeliveryWorkflow_productNotAvailable_fails() {
        // Create a product with insufficient stock
        ProductRequestDTO lowStockProduct = ProductRequestDTO.builder()
                .code("PROD-LOW-STOCK")
                .name("Low Stock Product")
                .description("Product with low stock")
                .category("Test")
                .productionTime(30)
                .cost(10.0)
                .stock(0.0)
                .stockMin(5.0)
                .build();
        ProductResponseDTO product = productService.createProduct(lowStockProduct);

        // Try to create order with unavailable product
        DeliveryOrderLineRequestDTO line = DeliveryOrderLineRequestDTO.builder()
                .productId(product.getId())
                .quantity(1)
                .unitPrice(15.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-TEST-FAIL-001")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .deliveryAddress("Test Address")
                .deliveryCity("Test City")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        // Should fail due to product not being available
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> deliveryOrderService.create(orderRequest)
        );

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
    }

    @Test
    void testDeliveryWorkflow_duplicateDeliveryForOrder_fails() {
        // Create a delivery order
        DeliveryOrderLineRequestDTO line = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(2)
                .unitPrice(60.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-TEST-002")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .deliveryAddress("Test Address")
                .deliveryCity("Test City")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        DeliveryOrderResponseDTO order = deliveryOrderService.create(orderRequest);

        // Create first delivery
        DeliveryRequestDTO delivery1Request = DeliveryRequestDTO.builder()
                .deliveryNumber("DEL-TEST-002")
                .deliveryOrderId(order.getId())
                .vehicle("Van-001")
                .driver("Driver 1")
                .driverPhone("+212600111111")
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(LocalDate.now().plusDays(2))
                .cost(50.0)
                .trackingNumber("TRK-002")
                .build();

        deliveryService.create(delivery1Request);

        // Try to create duplicate delivery for same order
        DeliveryRequestDTO delivery2Request = DeliveryRequestDTO.builder()
                .deliveryNumber("DEL-TEST-003")
                .deliveryOrderId(order.getId())
                .vehicle("Van-002")
                .driver("Driver 2")
                .driverPhone("+212600222222")
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(LocalDate.now().plusDays(2))
                .cost(50.0)
                .trackingNumber("TRK-003")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> deliveryService.create(delivery2Request)
        );

        assertTrue(exception.getMessage().contains("Cette commande a déjà une livraison associée"));
    }

    @Test
    void testDeliveryWorkflow_invalidStatusTransition_fails() {
        // Create order and delivery
        DeliveryOrderLineRequestDTO line = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(1)
                .unitPrice(60.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-TEST-003")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .deliveryAddress("Test Address")
                .deliveryCity("Test City")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        DeliveryOrderResponseDTO order = deliveryOrderService.create(orderRequest);

        // First set order to LIVREE
        deliveryOrderService.updateStatus(order.getId(), OrderStatus.LIVREE);

        // Try to change status after delivery - should fail
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> deliveryOrderService.updateStatus(order.getId(), OrderStatus.EN_ROUTE)
        );

        assertTrue(exception.getMessage().contains("Impossible de modifier le statut d'une commande déjà livrée"));
    }

    @Test
    void testDeliveryWorkflow_searchAndFilterOperations() {
        // Create multiple customers
        CustomerRequestDTO customer2Request = CustomerRequestDTO.builder()
                .code("CUST-TEST-002")
                .name("TechCorp Inc")
                .contact("John Doe")
                .phone("+212600333444")
                .email("john@techcorp.com")
                .address("20 Tech Street")
                .city("Rabat")
                .postalCode("10000")
                .country("Morocco")
                .build();
        CustomerResponseDTO customer2 = customerService.create(customer2Request);

        // Search customers
        Page<CustomerResponseDTO> casaCustomers = customerService.getByCity("Casablanca", PageRequest.of(0, 20));
        assertEquals(1, casaCustomers.getTotalElements());
        assertEquals("ACME Corp Test", casaCustomers.getContent().get(0).getName());

        Page<CustomerResponseDTO> rabatCustomers = customerService.getByCity("Rabat", PageRequest.of(0, 20));
        assertEquals(1, rabatCustomers.getTotalElements());
        assertEquals("TechCorp Inc", rabatCustomers.getContent().get(0).getName());

        Page<CustomerResponseDTO> moroccoCustomers = customerService.getByCountry("Morocco", PageRequest.of(0, 20));
        assertEquals(2, moroccoCustomers.getTotalElements());

        // Create orders for different customers
        DeliveryOrderLineRequestDTO line = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(1)
                .unitPrice(60.0)
                .build();

        DeliveryOrderRequestDTO order1Request = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-FILTER-001")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(1))
                .deliveryAddress("Address 1")
                .deliveryCity("City 1")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        DeliveryOrderRequestDTO order2Request = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-FILTER-002")
                .customerId(customer2.getId())
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(5))
                .deliveryAddress("Address 2")
                .deliveryCity("City 2")
                .deliveryPostalCode("20000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        deliveryOrderService.create(order1Request);
        DeliveryOrderResponseDTO order2 = deliveryOrderService.create(order2Request);
        deliveryOrderService.updateStatus(order2.getId(), OrderStatus.EN_ROUTE);

        // Filter orders by status
        Page<DeliveryOrderResponseDTO> preparationOrders = deliveryOrderService.getByStatus(OrderStatus.EN_PREPARATION, PageRequest.of(0, 20));
        assertTrue(preparationOrders.getTotalElements() >= 1);

        Page<DeliveryOrderResponseDTO> enRouteOrders = deliveryOrderService.getByStatus(OrderStatus.EN_ROUTE, PageRequest.of(0, 20));
        assertTrue(enRouteOrders.getTotalElements() >= 1);

        // Filter orders by customer
        Page<DeliveryOrderResponseDTO> customer1Orders = deliveryOrderService.getByCustomer(customerId, PageRequest.of(0, 20));
        assertTrue(customer1Orders.getTotalElements() >= 1);
    }

    @Test
    void testDeliveryWorkflow_cascadeDeleteOrderDeletesLines() {
        // Create order with lines
        DeliveryOrderLineRequestDTO line1 = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(2)
                .unitPrice(60.0)
                .build();

        DeliveryOrderLineRequestDTO line2 = DeliveryOrderLineRequestDTO.builder()
                .productId(product2Id)
                .quantity(1)
                .unitPrice(30.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-CASCADE-001")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .deliveryAddress("Test Address")
                .deliveryCity("Test City")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(Arrays.asList(line1, line2))
                .build();

        DeliveryOrderResponseDTO order = deliveryOrderService.create(orderRequest);
        Long orderId = order.getId();

        // Verify order has 2 lines
        assertEquals(2, order.getOrderLines().size());

        // Delete the order - lines should cascade delete
        deliveryOrderService.delete(orderId);

        // Verify order no longer exists
        assertThrows(RuntimeException.class, () -> deliveryOrderService.getById(orderId));
    }

    @Test
    void testDeliveryWorkflow_trackingByDeliveryNumber() {
        // Create order and delivery
        DeliveryOrderLineRequestDTO line = DeliveryOrderLineRequestDTO.builder()
                .productId(product1Id)
                .quantity(1)
                .unitPrice(60.0)
                .build();

        DeliveryOrderRequestDTO orderRequest = DeliveryOrderRequestDTO.builder()
                .orderNumber("ORD-TRACK-001")
                .customerId(customerId)
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(2))
                .deliveryAddress("Test Address")
                .deliveryCity("Test City")
                .deliveryPostalCode("10000")
                .status(OrderStatus.EN_PREPARATION)
                .orderLines(List.of(line))
                .build();

        DeliveryOrderResponseDTO order = deliveryOrderService.create(orderRequest);

        DeliveryRequestDTO deliveryRequest = DeliveryRequestDTO.builder()
                .deliveryNumber("DEL-TRACK-001")
                .deliveryOrderId(order.getId())
                .vehicle("Van-123")
                .driver("Mohamed")
                .driverPhone("+212600555666")
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(LocalDate.now().plusDays(2))
                .cost(45.0)
                .trackingNumber("TRK-TRACK-001")
                .build();

        DeliveryResponseDTO delivery = deliveryService.create(deliveryRequest);

        // Find delivery by delivery number
        DeliveryResponseDTO foundByNumber = deliveryService.getByDeliveryNumber("DEL-TRACK-001");
        assertNotNull(foundByNumber);
        assertEquals(delivery.getId(), foundByNumber.getId());

        // Find delivery by tracking number
        DeliveryResponseDTO foundByTracking = deliveryService.getByTrackingNumber("TRK-TRACK-001");
        assertNotNull(foundByTracking);
        assertEquals(delivery.getId(), foundByTracking.getId());

        // Find delivery by order
        DeliveryResponseDTO foundByOrder = deliveryService.getByDeliveryOrder(order.getId());
        assertNotNull(foundByOrder);
        assertEquals(delivery.getId(), foundByOrder.getId());
    }
}
