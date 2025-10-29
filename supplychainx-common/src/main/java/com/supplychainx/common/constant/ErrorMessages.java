package com.othman.exemple.common.constant;


public final class ErrorMessages {

    private ErrorMessages() {}

    // Generic
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String INVALID_INPUT = "Invalid input provided";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Access forbidden";
    
    // Supply
    public static final String SUPPLIER_NOT_FOUND = "Supplier not found";
    public static final String SUPPLIER_HAS_ACTIVE_ORDERS = "Cannot delete supplier with active orders";
    public static final String RAW_MATERIAL_NOT_FOUND = "Raw material not found";
    public static final String RAW_MATERIAL_IN_USE = "Cannot delete raw material that is in use";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock";
    
    // Production
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String PRODUCT_HAS_ORDERS = "Cannot delete product with production orders";
    public static final String PRODUCTION_ORDER_NOT_FOUND = "Production order not found";
    public static final String MATERIALS_NOT_AVAILABLE = "Required materials not available";
    
    // Delivery
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String CUSTOMER_HAS_ACTIVE_ORDERS = "Cannot delete customer with active orders";
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String DELIVERY_NOT_FOUND = "Delivery not found";
    
    // Security
    public static final String USER_NOT_FOUND = "User not found";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
}
