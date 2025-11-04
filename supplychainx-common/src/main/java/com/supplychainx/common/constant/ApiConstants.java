package com.supplychainx.common.constant;


public final class ApiConstants {

    private ApiConstants() {}

    // API Base Paths
    public static final String API_V1 = "/api/v1";
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIR = "asc";
    
    // Module paths
    public static final String SUPPLY_BASE = API_V1 + "/supply";
    public static final String PRODUCTION_BASE = API_V1 + "/production";
    public static final String DELIVERY_BASE = API_V1 + "/delivery";
    public static final String SECURITY_BASE = API_V1 + "/auth";
    public static final String AUDIT_BASE = API_V1 + "/audit";
}
