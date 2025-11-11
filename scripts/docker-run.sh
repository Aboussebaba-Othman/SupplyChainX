#!/bin/bash

# Docker Run Script
# Starts all services with docker-compose

set -e

COMMAND="${1:-up}"

echo "=================================="
echo "  Docker Compose Script"
echo "=================================="
echo ""

case "$COMMAND" in
    up)
        echo "Starting all services..."
        docker-compose up -d
        echo ""
        echo "Services started!"
        echo "- Application: http://localhost:8081"
        echo "- phpMyAdmin: http://localhost:8082"
        echo "- SonarQube: http://localhost:9000"
        ;;
    down)
        echo "Stopping all services..."
        docker-compose down
        echo "Services stopped!"
        ;;
    restart)
        echo "Restarting all services..."
        docker-compose restart
        echo "Services restarted!"
        ;;
    logs)
        docker-compose logs -f
        ;;
    status)
        docker-compose ps
        ;;
    *)
        echo "Usage: $0 {up|down|restart|logs|status}"
        exit 1
        ;;
esac

echo "=================================="
