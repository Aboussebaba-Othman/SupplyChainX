#!/bin/bash

# Script to check ELK stack health for SupplyChainX

echo "========================================"
echo "SupplyChainX ELK Stack Health Check"
echo "========================================"
echo ""

# Check Elasticsearch
echo "Elasticsearch:"
if curl -s http://localhost:9200/_cluster/health | grep -q "green\|yellow"; then
    echo "  Status: ✓ Healthy"
    curl -s http://localhost:9200/_cluster/health | grep -o '"status":"[^"]*"'
else
    echo "  Status: ✗ Not responding"
fi
echo ""

# Check Logstash
echo "Logstash:"
if curl -s http://localhost:9600/_node/stats > /dev/null 2>&1; then
    echo "  Status: ✓ Healthy"
else
    echo "  Status: ✗ Not responding"
fi
echo ""

# Check Kibana
echo "Kibana:"
if curl -s http://localhost:5601/api/status | grep -q "available"; then
    echo "  Status: ✓ Healthy"
else
    echo "  Status: ✗ Not responding"
fi
echo ""

# Check log count
echo "Log Statistics:"
LOG_COUNT=$(curl -s "http://localhost:9200/supplychainx-logs-*/_count" | grep -o '"count":[0-9]*' | cut -d: -f2)
echo "  Total logs: ${LOG_COUNT:-0}"
echo ""

# Check containers
echo "Container Status:"
docker compose -f docker-compose-elk.yml ps
echo ""
