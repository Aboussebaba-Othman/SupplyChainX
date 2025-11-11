#!/bin/bash

# Docker Build Script
# Builds the SupplyChainX Docker image

set -e

IMAGE_NAME="supplychainx"
IMAGE_TAG="${1:-latest}"

echo "=================================="
echo "  Docker Build Script"
echo "=================================="
echo ""
echo "Building image: $IMAGE_NAME:$IMAGE_TAG"
echo ""

docker build -t "$IMAGE_NAME:$IMAGE_TAG" .

echo ""
echo "=================================="
echo "✓ Build complete!"
echo "Image: $IMAGE_NAME:$IMAGE_TAG"
echo ""
echo "To run the container:"
echo "  docker-compose up -d"
echo "=================================="
