#!/bin/bash

# SonarQube Analysis Script
# Usage: ./scripts/sonar-analyze.sh [SONAR_TOKEN]

set -e

SONAR_HOST_URL="http://localhost:9000"
SONAR_PROJECT_KEY="supplychainx"
SONAR_TOKEN="${1:-}"

echo "=================================="
echo "  SonarQube Analysis Script"
echo "=================================="
echo ""

# Check if SonarQube is running
if ! curl -s -o /dev/null -w "%{http_code}" "$SONAR_HOST_URL" | grep -q "200"; then
    echo "Error: SonarQube is not running at $SONAR_HOST_URL"
    echo "Start it with: docker-compose up -d sonarqube"
    exit 1
fi

echo "✓ SonarQube is running"
echo ""

# Check if token is provided
if [ -z "$SONAR_TOKEN" ]; then
    echo "Usage: $0 <SONAR_TOKEN>"
    echo ""
    echo "To get a token:"
    echo "1. Open http://localhost:9000"
    echo "2. Login (admin/admin)"
    echo "3. Go to: My Account > Security > Generate Token"
    echo "4. Copy the token and run: $0 <your-token>"
    exit 1
fi

echo "Step 1: Clean and build project..."
mvn clean install -DskipTests

echo ""
echo "Step 2: Run tests and generate coverage..."
mvn test jacoco:report

echo ""
echo "Step 3: Run SonarQube analysis..."
mvn sonar:sonar \
    -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
    -Dsonar.host.url="$SONAR_HOST_URL" \
    -Dsonar.login="$SONAR_TOKEN"

echo ""
echo "=================================="
echo "✓ Analysis complete!"
echo "View results at: $SONAR_HOST_URL/dashboard?id=$SONAR_PROJECT_KEY"
echo "=================================="
