#!/bin/bash

# Test Script
# Runs tests with coverage

set -e

PROFILE="${1:-test}"

echo "=================================="
echo "  Test Script"
echo "=================================="
echo ""
echo "Running tests with profile: $PROFILE"
echo ""

mvn clean test -P"$PROFILE"

echo ""
echo "Generating coverage report..."
mvn jacoco:report

echo ""
echo "=================================="
echo "✓ Tests complete!"
echo "Coverage report: target/site/jacoco/index.html"
echo "=================================="
