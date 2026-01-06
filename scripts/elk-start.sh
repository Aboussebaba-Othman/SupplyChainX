#!/bin/bash

# Script to start ELK stack for SupplyChainX
set -e

echo "========================================"
echo "Starting SupplyChainX ELK Stack"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running"
    exit 1
fi

# Create network if it doesn't exist
echo "Creating Docker network..."
docker network create supplychainx-network 2>/dev/null || echo "Network already exists"

# Start ELK services
echo "Starting ELK services..."
docker compose -f docker-compose-elk.yml up -d

# Wait for Elasticsearch
echo "Waiting for Elasticsearch to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:9200/_cluster/health > /dev/null 2>&1; then
        echo "Elasticsearch is ready!"
        break
    fi
    echo "Waiting... ($i/30)"
    sleep 2
done

# Create index template
echo "Creating index template..."
curl -X PUT "localhost:9200/_index_template/supplychainx-logs" -H 'Content-Type: application/json' -d'
{
  "index_patterns": ["supplychainx-logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.refresh_interval": "5s"
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "level": { "type": "keyword" },
        "logger_name": { "type": "keyword" },
        "message": { "type": "text" },
        "app_name": { "type": "keyword" },
        "environment": { "type": "keyword" },
        "userId": { "type": "keyword" },
        "username": { "type": "keyword" },
        "userRole": { "type": "keyword" },
        "orderId": { "type": "keyword" },
        "productId": { "type": "keyword" },
        "supplierId": { "type": "keyword" },
        "httpMethod": { "type": "keyword" },
        "httpPath": { "type": "keyword" },
        "httpStatus": { "type": "integer" },
        "tags": { "type": "keyword" }
      }
    }
  }
}'
echo ""

# Wait for Kibana
echo "Waiting for Kibana to be ready..."
for i in {1..60}; do
    if curl -s http://localhost:5601/api/status > /dev/null 2>&1; then
        echo "Kibana is ready!"
        break
    fi
    echo "Waiting... ($i/60)"
    sleep 2
done

echo ""
echo "========================================"
echo "ELK Stack Started Successfully!"
echo "========================================"
echo ""
echo "Access points:"
echo "  - Elasticsearch: http://localhost:9200"
echo "  - Kibana:        http://localhost:5601"
echo "  - Logstash:      tcp://localhost:5000"
echo ""
echo "Useful commands:"
echo "  - View logs:     docker compose -f docker-compose-elk.yml logs -f"
echo "  - Stop ELK:      ./scripts/elk-stop.sh"
echo "  - Check health:  ./scripts/elk-health.sh"
echo ""
