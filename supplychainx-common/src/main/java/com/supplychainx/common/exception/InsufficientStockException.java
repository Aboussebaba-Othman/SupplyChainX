package com.supplychainx.common.exception;


public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String itemName, int available, int required) {
        super(String.format("Insufficient stock for %s. Available: %d, Required: %d", 
                itemName, available, required));
    }
}
