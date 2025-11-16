# SupplyChainX - DevOps Documentation

## Quick Start Scripts

### Build & Test

```bash
# Build with dev profile
./scripts/build.sh dev

# Build for production
./scripts/build.sh prod

# Run tests with coverage
./scripts/test.sh
```

### Docker Operations

```bash
# Start all services
./scripts/docker-run.sh up

# Stop all services
./scripts/docker-run.sh down

# View logs
./scripts/docker-run.sh logs

# Check status
./scripts/docker-run.sh status

# Build Docker image
./scripts/docker-build.sh latest
```

### SonarQube Analysis

```bash
# Run code quality analysis
./scripts/sonar-analyze.sh <YOUR_SONAR_TOKEN>
```

## Services URLs

- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **phpMyAdmin**: http://localhost:8082
- **SonarQube**: http://localhost:9000

## Maven Profiles

### Development Profile (default)
```bash
mvn clean install -Pdev
```
- MySQL local
- Debug logging
- Tests enabled
- Liquibase enabled

### Test Profile
```bash
mvn clean install -Ptest
```
- H2 in-memory database
- JaCoCo coverage enabled
- Full test execution

### Production Profile
```bash
mvn clean install -Pprod
```
- Production database config
- Optimized compilation
- Minimal logging
- Tests skipped

## SonarQube Setup

### 1. Start SonarQube
```bash
docker-compose up -d sonarqube
```

### 2. Access & Login
- URL: http://localhost:9000
- Default: admin/admin (change on first login)

### 3. Create Project
1. Click "Create a local project"
2. Project key: `supplychainx`
3. Display name: `SupplyChainX`
4. Generate token

### 4. Run Analysis
```bash
./scripts/sonar-analyze.sh <YOUR_TOKEN>
```

## Code Coverage

### Generate Reports
```bash
mvn clean test jacoco:report
```

### View Reports
- JaCoCo: `target/site/jacoco/index.html`
- Minimum coverage: 60%
- Exclusions: DTOs, entities, mappers, config classes

## CI/CD Pipeline

### GitHub Actions
Pipeline runs automatically on:
- Push to `dev`, `main`, `feature/**` branches
- Pull requests to `dev` or `main`

### Jobs
1. **build-and-test**: Maven build, unit tests, JaCoCo coverage
2. **sonarqube-analysis**: Code quality scan (dev/main only)
3. **docker-build**: Docker image build (dev/main only)

### Required Secrets
Add in GitHub Settings > Secrets:
- `SONAR_TOKEN`: SonarQube authentication token
- `SONAR_HOST_URL`: SonarQube server URL

## Troubleshooting

### Port Already in Use
```bash
# Stop all containers
docker-compose down

# Check port usage
lsof -i :8081
lsof -i :9000
```

### SonarQube Not Starting
```bash
# Check logs
docker-compose logs sonarqube

# Wait 2 minutes for full startup
# Check health
curl http://localhost:9000/api/system/status
```

### Database Connection Issues
```bash
# Verify MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Reset database
docker-compose down -v
docker-compose up -d mysql
```

### Build Failures
```bash
# Clean Maven cache
mvn clean

# Clear local repository
rm -rf ~/.m2/repository/com/supplychainx

# Rebuild
mvn clean install -U
```

## Project Structure

```
SupplyChainX/
├── .github/workflows/       # CI/CD pipelines
├── scripts/                 # Helper scripts
│   ├── build.sh
│   ├── test.sh
│   ├── docker-build.sh
│   ├── docker-run.sh
│   └── sonar-analyze.sh
├── supplychainx-common/     # Shared utilities
├── supplychainx-security/   # Authentication & authorization
├── supplychainx-supply/     # Raw materials management
├── supplychainx-production/ # Production planning
├── supplychainx-delivery/   # Delivery tracking
├── supplychainx-audit/      # Audit & notifications
├── supplychainx-integration/# External integrations
├── supplychainx-app/        # Main application
├── docker-compose.yml       # Service orchestration
├── Dockerfile               # Multi-stage build
└── pom.xml                  # Parent POM
```

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Docker Documentation](https://docs.docker.com/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
