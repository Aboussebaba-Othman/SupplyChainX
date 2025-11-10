#!/bin/bash

# ==============================================
# SupplyChainX - Application Startup Script
# ==============================================

# Database Configuration
export DB_USERNAME=supplychainx_user
export DB_PASSWORD=supplychainx_password

# Email/SMTP Configuration (for stock alerts)
# Configured in application.yml:
#   - SMTP: paino49sukuna@gmail.com
#   - Recipient: aaboussebaba.othman@gmail.com
export ALERT_EMAIL_ENABLED=true  # ✅ Email alerts activated

# Start the application
mvn spring-boot:run -pl supplychainx-app
