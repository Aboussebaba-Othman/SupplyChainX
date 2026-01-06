#!/bin/bash

# Script to stop ELK stack for SupplyChainX
set -e

echo "========================================"
echo "Stopping SupplyChainX ELK Stack"
echo "========================================"

docker compose -f docker-compose-elk.yml down

echo ""
echo "ELK Stack stopped successfully!"
echo ""
echo "To remove volumes (WARNING: This deletes all data):"
echo "  docker compose -f docker-compose-elk.yml down -v"
echo ""
