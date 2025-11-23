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

SELECT 'Database initialization completed' AS status;
