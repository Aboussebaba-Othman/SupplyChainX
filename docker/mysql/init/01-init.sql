-- Initial database setup for SupplyChainX
-- This script runs automatically when MySQL container starts for the first time

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS supplychainx_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE supplychainx_db;

-- Grant privileges to application user
GRANT ALL PRIVILEGES ON supplychainx_db.* TO 'supplychainx_user'@'%';
FLUSH PRIVILEGES;

-- Create a test user for development (optional)
-- This will be managed by Liquibase in production
-- Uncomment if needed for initial testing
-- INSERT INTO users (id, email, password, first_name, last_name, role, is_active, created_at, updated_at)
-- VALUES (1, 'admin@supplychainx.com', '$2a$10$...', 'Admin', 'User', 'ADMIN', true, NOW(), NOW());

SELECT 'Database initialization completed' AS status;
