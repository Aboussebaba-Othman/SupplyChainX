-- Cleanup script executed before each test to ensure test isolation
-- This script is referenced by @Sql annotation in BaseSecurityIntegrationTest

-- Delete all refresh tokens
DELETE FROM refresh_tokens;

-- Delete test users (keep the 10 Liquibase users)
DELETE FROM users
WHERE username NOT IN (
    'admin',
    'supply_manager',
    'purchase_manager',
    'logistics_supervisor',
    'production_manager',
    'planner',
    'production_supervisor',
    'sales_manager',
    'delivery_logistics',
    'delivery_supervisor'
);
