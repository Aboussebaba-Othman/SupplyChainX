#!/bin/bash

# Build Script
# Builds the project with specified profile

set -e

PROFILE="${1:-dev}"

echo "=================================="
echo "  Build Script"
echo "=================================="
echo ""
echo "Building with profile: $PROFILE"
echo ""

mvn clean install -P"$PROFILE"

echo ""
echo "=================================="
echo "✓ Build complete!"
echo "JAR location: supplychainx-app/target/"
echo "=================================="
