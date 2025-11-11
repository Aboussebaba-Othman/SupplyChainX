# SupplyChainX - Docker Setup Guide

## 🐳 Docker Services

This project uses Docker Compose to manage the following services:

- **MySQL 8.0**: Main database
- **phpMyAdmin**: Web-based database management tool
- **Mailhog**: Email testing tool (SMTP server + web UI)
- **Spring Boot App**: The main application (to be deployed)

## 📋 Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+

## 🚀 Quick Start

### 1. Start All Services

```bash
# Start MySQL, phpMyAdmin, and Mailhog
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### 2. Access Services

- **phpMyAdmin**: http://localhost:8080
  - Server: `mysql`
  - Username: `root`
  - Password: `rootpassword`
  
- **MySQL Database**:
  - Host: `localhost`
  - Port: `3306`
  - Database: `supplychainx_db`
  - User: `supplychainx_user`
  - Password: `supplychainx_password`

- **Mailhog UI**: http://localhost:8025
  - SMTP Server: `localhost:1025`

### 3. Stop Services

```bash
# Stop all services
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (⚠️ deletes all data)
docker-compose down -v
```

## 🔧 Configuration

### Environment Variables

Copy `.env.example` to `.env` and customize:

```bash
cp .env.example .env
```

Edit `.env`:
```env
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=supplychainx_db
MYSQL_USER=supplychainx_user
MYSQL_PASSWORD=your_secure_password
```

### Custom MySQL Initialization

Place SQL scripts in `docker/mysql/init/` - they will run automatically on first startup:

```
docker/mysql/init/
├── 01-init.sql
├── 02-seed-data.sql
└── 03-test-users.sql
```

## 🏗️ Building and Running the Application

### Development Mode (Local)

```bash
# Run with local IDE, connect to Docker MySQL
mvn spring-boot:run -pl supplychainx-app -am
```
²
Application will connect to MySQL at `localhost:3306`

### Production Mode (Docker)

1. **Uncomment the `supplychainx-app` service** in `docker-compose.yml`

2. **Build and run**:
```bash
docker-compose up -d --build supplychainx-app
```

3. **Access application**:
   - API: http://localhost:8090/api
   - Swagger UI: http://localhost:8090/swagger-ui.html
   - Actuator: http://localhost:8090/actuator/health

## 📊 Database Management

### Using phpMyAdmin

1. Open http://localhost:8080
2. Login with root credentials
3. Select `supplychainx_db`
4. View tables, run queries, export/import data

### Using MySQL Client

```bash
# Connect via docker-compose
docker-compose exec mysql mysql -u root -p

# Or connect directly
mysql -h localhost -P 3306 -u supplychainx_user -p supplychainx_db
```

### Backup Database

```bash
# Export database
docker-compose exec mysql mysqldump -u root -prootpassword supplychainx_db > backup.sql

# Import database
docker-compose exec -T mysql mysql -u root -prootpassword supplychainx_db < backup.sql
```

## 📧 Email Testing with Mailhog

Mailhog captures all emails sent by the application:

1. Configure application to use SMTP: `localhost:1025`
2. View emails at: http://localhost:8025
3. All emails are caught (not sent to real addresses)

## 🐛 Troubleshooting

### MySQL Container Won't Start

```bash
# Check logs
docker-compose logs mysql

# Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Port Already in Use

```bash
# Check what's using port 3306
sudo lsof -i :3306

# Or change port in docker-compose.yml
ports:
  - "3307:3306"  # Use 3307 instead
```

### Application Can't Connect to Database

```bash
# Verify MySQL is healthy
docker-compose ps

# Check network
docker network inspect supplychainx_supplychainx-network

# View application logs
docker-compose logs supplychainx-app
```

### Reset Everything

```bash
# Nuclear option: delete everything and start fresh
docker-compose down -v
docker system prune -a
docker-compose up -d
```

## 🔐 Production Considerations

⚠️ **Before deploying to production:**

1. **Change all default passwords** in `.env`
2. **Use Docker secrets** for sensitive data
3. **Enable SSL/TLS** for MySQL connections
4. **Remove phpMyAdmin** (or restrict access)
5. **Remove Mailhog** (use real SMTP server)
6. **Configure backup strategy** for MySQL volumes
7. **Set resource limits** in docker-compose.yml:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2'
         memory: 2G
   ```

## 📦 Volumes

- `mysql_data`: Persists MySQL database files
- `app_logs`: Application log files

To backup volumes:
```bash
docker run --rm -v supplychainx_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz /data
```

## 🌐 Network

All services run on `supplychainx-network` bridge network.

Services can communicate using container names:
- Application → MySQL: `jdbc:mysql://mysql:3306/supplychainx_db`
- Application → Mailhog: `mailhog:1025`

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
