#!/bin/bash

# Database Configuration
export DB_USERNAME=supplychainx_user
export DB_PASSWORD=supplychainx_password

# Configured in application.yml:
export ALERT_EMAIL_ENABLED=true

# Start the application
mvn spring-boot:run -pl supplychainx-app
