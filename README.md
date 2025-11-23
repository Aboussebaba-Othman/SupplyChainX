# SupplyChainX — Quick dev guide

This repository contains a multi-module Spring Boot application for SupplyChainX.

Quick commands

  
[![CI](https://github.com/Aboussebaba-Othman/SupplyChainX/actions/workflows/ci.yml/badge.svg?branch=supplychainx-production)](https://github.com/Aboussebaba-Othman/SupplyChainX/actions/workflows/ci.yml)

  ./mvnw clean package -DskipTests

- Run unit tests for the production module only (fast):

  ./mvnw -pl supplychainx-production test

- Run integration tests (complete workflow with H2 in-memory database):

  ./mvnw -pl supplychainx-app test -Dtest=ProductionWorkflowIntegrationTest

- Run all tests (unit + integration):

  ./mvnw test

- Run the application (jar produced under supplychainx-app/target):

  java -jar supplychainx-app/target/supplychainx-app-1.0.0-SNAPSHOT.jar

- Run tests (integration test added in main module):

  ./mvnw test -Dtest=ProductionWorkflowIntegrationTest

Notes

- The app by default listens on port configured in `application.properties` (check `src/main/resources/application.properties`).
- Actuator health is available at `/actuator/health` when the app is running.

If you want me to add a Postman environment/export or CI job to run these tests automatically, say so and I'll add it.
# 🏭 SupplyChainX - Système de Gestion de Chaîne d'Approvisionnement

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Description

SupplyChainX est une application complète de gestion de chaîne d'approvisionnement développée avec Spring Boot. Elle gère l'ensemble du cycle : approvisionnement en matières premières, production de produits finis, et gestion des stocks.

### 🎯 Fonctionnalités Principales

- **Module Supply (Approvisionnement)**
  - Gestion des fournisseurs
  - Gestion des matières premières
  - Commandes d'approvisionnement
  - Suivi des stocks de matières premières

- **Module Production**
  - Catalogue de produits
  - Nomenclatures (Bills of Material)
  - Ordres de production
  - Workflow complet de production (Planification → Production → Terminé)
  - Gestion automatique des stocks

- **Module Delivery (Livraison)**
  - Gestion des clients
  - Commandes de livraison multi-produits
  - Lignes de commande avec validation des stocks
  - Gestion des livraisons physiques
  - Suivi de livraison (tracking)
  - Workflow complet (Préparation → En route → Livrée)

## 🏗️ Architecture

### Structure Multi-Module Maven

```
SupplyChainX/
├── supplychainx-common/          # Classes communes (entités de base, utils)
├── supplychainx-security/        # Configuration de sécurité
├── supplychainx-supply/          # Module Approvisionnement
├── supplychainx-production/      # Module Production
├── supplychainx-delivery/        # Module Livraison
└── supplychainx-app/             # Application principale (point d'entrée)
```

### Technologies Utilisées

- **Backend:** Spring Boot 3.2.0, Spring Data JPA, Spring Web
- **Base de données:** MySQL 8.0
- **Migration:** Liquibase
- **Documentation API:** Swagger/OpenAPI 3.0
- **Validation:** Jakarta Validation
- **Mapping:** MapStruct
- **Build:** Maven

## 🚀 Installation et Démarrage

### Prérequis

- Java 17+
- MySQL 8.0+
- Maven 3.8+

### 1. Configuration de la Base de Données

```sql
CREATE DATABASE supplychainx_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'supplychainx_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON supplychainx_db.* TO 'supplychainx_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configuration Application

Éditez le fichier `supplychainx-app/src/main/resources/application.properties` :

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/supplychainx_db
spring.datasource.username=supplychainx_user
spring.datasource.password=your_password

# Server Port
server.port=8081

# Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
```

### 3. Compilation et Lancement

```bash
# Cloner le repository
git clone https://github.com/Aboussebaba-Othman/SupplyChainX.git
cd SupplyChainX

# Compiler le projet
mvn clean install -DskipTests

# Lancer l'application
cd supplychainx-app/target
java -jar supplychainx-app-1.0.0-SNAPSHOT.jar

# OU directement avec Maven
mvn spring-boot:run -pl supplychainx-app
```

L'application sera accessible sur : **http://localhost:8081**

### 4. Vérification

```bash
# Health check
curl http://localhost:8081/actuator/health

# Swagger UI
http://localhost:8081/swagger-ui/index.html
```

## 📚 API Documentation

### Base URL
```
http://localhost:8081
```

### Endpoints Principaux

#### 🏪 **Supply Module - Fournisseurs**
```
POST   /api/supply/suppliers              # Créer un fournisseur
GET    /api/supply/suppliers              # Liste des fournisseurs
GET    /api/supply/suppliers/{id}         # Détails d'un fournisseur
PUT    /api/supply/suppliers/{id}         # Modifier un fournisseur
DELETE /api/supply/suppliers/{id}         # Supprimer un fournisseur
```

#### 📦 **Supply Module - Matières Premières**
```
POST   /api/supply/raw-materials          # Créer une matière première
GET    /api/supply/raw-materials          # Liste des matières premières
GET    /api/supply/raw-materials/{id}     # Détails d'une matière
PUT    /api/supply/raw-materials/{id}     # Modifier une matière
PATCH  /api/supply/raw-materials/{id}/add-stock    # Ajouter du stock
PATCH  /api/supply/raw-materials/{id}/reduce-stock # Réduire du stock
```

#### 📋 **Supply Module - Commandes d'Approvisionnement**
```
POST   /api/supply/orders                 # Créer une commande
GET    /api/supply/orders                 # Liste des commandes
GET    /api/supply/orders/{id}            # Détails d'une commande
PUT    /api/supply/orders/{id}            # Modifier une commande
PATCH  /api/supply/orders/{id}/validate   # Valider une commande
PATCH  /api/supply/orders/{id}/receive    # Réceptionner une commande
```

#### 🏭 **Production Module - Produits**
```
POST   /api/production/products           # Créer un produit
GET    /api/production/products           # Liste des produits
GET    /api/production/products/{id}      # Détails d'un produit
PUT    /api/production/products/{id}      # Modifier un produit
GET    /api/production/products/search    # Rechercher des produits
```

#### 📝 **Production Module - Nomenclatures (BOM)**
```
POST   /api/production/bills-of-material  # Créer une nomenclature
GET    /api/production/bills-of-material  # Liste des nomenclatures
GET    /api/production/bills-of-material/product/{productId}  # Recette d'un produit
PUT    /api/production/bills-of-material/{id}                 # Modifier une nomenclature
DELETE /api/production/bills-of-material/{id}                 # Supprimer une nomenclature
```

#### 🔧 **Production Module - Ordres de Production**
```
POST   /api/production/production-orders  # Créer un ordre
GET    /api/production/production-orders  # Liste des ordres
GET    /api/production/production-orders/{id}        # Détails d'un ordre
GET    /api/production/production-orders/order-number/{number}  # Par numéro
PUT    /api/production/production-orders/{id}        # Modifier un ordre
PATCH  /api/production/production-orders/{id}/start  # Démarrer la production
PATCH  /api/production/production-orders/{id}/complete # Terminer la production
PATCH  /api/production/production-orders/{id}/cancel   # Annuler un ordre
```

#### 🚚 **Delivery Module - Clients**
```
POST   /api/delivery/customers            # Créer un client
GET    /api/delivery/customers            # Liste des clients (paginée)
GET    /api/delivery/customers/{id}       # Détails d'un client
GET    /api/delivery/customers/code/{code} # Client par code
PUT    /api/delivery/customers/{id}       # Modifier un client
GET    /api/delivery/customers/search?keyword=xxx  # Rechercher des clients
GET    /api/delivery/customers/city/{city}         # Clients par ville
GET    /api/delivery/customers/country/{country}   # Clients par pays
DELETE /api/delivery/customers/{id}       # Supprimer un client
```

#### 📦 **Delivery Module - Commandes de Livraison**
```
POST   /api/delivery/orders               # Créer une commande (multi-produits)
GET    /api/delivery/orders               # Liste des commandes (paginée)
GET    /api/delivery/orders/{id}          # Détails d'une commande
GET    /api/delivery/orders/number/{orderNumber}  # Commande par numéro
PUT    /api/delivery/orders/{id}          # Modifier une commande
GET    /api/delivery/orders/status/{status}       # Commandes par statut
GET    /api/delivery/orders/customer/{customerId} # Commandes d'un client
GET    /api/delivery/orders/date-range?startDate&endDate  # Par période
GET    /api/delivery/orders/delayed       # Commandes en retard
PATCH  /api/delivery/orders/{id}/status?status=xxx # Changer le statut
DELETE /api/delivery/orders/{id}          # Supprimer une commande
```

#### 🚛 **Delivery Module - Livraisons**
```
POST   /api/delivery/deliveries           # Créer une livraison
GET    /api/delivery/deliveries           # Liste des livraisons (paginée)
GET    /api/delivery/deliveries/{id}      # Détails d'une livraison
GET    /api/delivery/deliveries/number/{deliveryNumber}  # Par numéro
GET    /api/delivery/deliveries/tracking/{trackingNumber} # Par tracking
GET    /api/delivery/deliveries/order/{deliveryOrderId}   # Livraison d'une commande
GET    /api/delivery/deliveries/status/{status}           # Par statut
GET    /api/delivery/deliveries/date/{date}               # Par date
GET    /api/delivery/deliveries/delayed    # Livraisons en retard
GET    /api/delivery/deliveries/driver/{driver}  # Livraisons d'un chauffeur
PATCH  /api/delivery/deliveries/{id}/status?status=xxx  # Changer le statut
PATCH  /api/delivery/deliveries/{id}/deliver            # Marquer comme livrée
DELETE /api/delivery/deliveries/{id}       # Supprimer une livraison
```

### 📄 Documentation Swagger

Documentation interactive complète disponible à :
```
http://localhost:8081/swagger-ui/index.html
```

## 🧪 Tests

### Structure des Tests

Le projet inclut deux types de tests automatisés :

1. **Tests Unitaires** (`supplychainx-production/src/test/java`)
   - Tests des services avec Mockito
   - Isolation complète des dépendances
   - Exécution ultra-rapide

2. **Tests d'Intégration** (`supplychainx-app/src/test/java`)
   - Tests end-to-end du workflow complet
   - Base H2 in-memory (configuration automatique avec profil `test`)
   - Validation des interactions entre modules

### Lancer les Tests

```bash
# Tests unitaires uniquement (module Production)
./mvnw -pl supplychainx-production test

# Tests d'intégration (workflow complet)
./mvnw -pl supplychainx-app test -Dtest=ProductionWorkflowIntegrationTest

# Tests d'intégration Delivery
./mvnw -pl supplychainx-app test -Dtest=DeliveryWorkflowIntegrationTest

# Tous les tests du projet
./mvnw test
```

### Scénarios de Tests d'Intégration

#### Production Workflow

Les tests d'intégration validentsle workflow complet Production:

1. **Workflow Nominal** (`testCompleteProductionWorkflow_success`)
   - Création Supplier → RawMaterial → Product → BOM
   - Création d'un ordre de production (10 unités)
   - Démarrage de la production (vérification stock matières)
   - Finalisation de la production (consommation matières + ajout produits)
   - Assertions : stocks mis à jour correctement

2. **Matières Insuffisantes** (`testProductionWorkflow_insufficientMaterials_fails`)
   - BOM nécessitant 2000 kg alors que seulement 1000 disponibles
   - Tentative de démarrage → exception attendue
   - Statut reste `EN_ATTENTE`

3. **État Invalide** (`testProductionWorkflow_cannotCompleteNonStartedOrder`)
   - Tentative de finaliser un ordre non démarré
   - Exception attendue avec message explicite

#### Delivery Workflow

Les tests d'intégration Delivery couvrent 7 scénarios:

1. **Workflow Complet** (`testCompleteDeliveryWorkflow_success`)
   - Création Client → Products → DeliveryOrder (multi-lignes) → Delivery
   - Transitions de statut : EN_PREPARATION → EN_ROUTE → LIVREE
   - Vérification des relations et calculs (totalAmount, tracking)

2. **Produit Indisponible** (`testDeliveryWorkflow_productNotAvailable_fails`)
   - Tentative de commande avec stock insuffisant
   - Exception BusinessException attendue

3. **Livraison Dupliquée** (`testDeliveryWorkflow_duplicateDeliveryForOrder_fails`)
   - Une seule livraison par commande autorisée
   - Exception si tentative de duplication

4. **Transition Invalide** (`testDeliveryWorkflow_invalidStatusTransition_fails`)
   - Impossible de modifier une commande déjà livrée
   - Validation des transitions de statut

5. **Recherche et Filtres** (`testDeliveryWorkflow_searchAndFilterOperations`)
   - Recherche clients par ville/pays
   - Filtrage commandes par statut/client
   - Pagination testée

6. **Suppression en Cascade** (`testDeliveryWorkflow_cascadeDeleteOrderDeletesLines`)
   - Suppression de commande supprime les lignes associées
   - Contraintes d'intégrité respectées

7. **Tracking** (`testDeliveryWorkflow_trackingByDeliveryNumber`)
   - Recherche par numéro de livraison
   - Recherche par numéro de tracking
   - Recherche par commande associée
```

### Scénarios de Tests d'Intégration

Les tests d'intégration valident le workflow complet :

1. **Workflow Nominal** (`testCompleteProductionWorkflow_success`)
   - Création Supplier → RawMaterial → Product → BOM
   - Création d'un ordre de production (10 unités)
   - Démarrage de la production (vérification stock matières)
   - Finalisation de la production (consommation matières + ajout produits)
   - Assertions : stocks mis à jour correctement

2. **Matières Insuffisantes** (`testProductionWorkflow_insufficientMaterials_fails`)
   - BOM nécessitant 2000 kg alors que seulement 1000 disponibles
   - Tentative de démarrage → exception attendue
   - Statut reste `EN_ATTENTE`

3. **État Invalide** (`testProductionWorkflow_cannotCompleteNonStartedOrder`)
   - Tentative de finaliser un ordre non démarré
   - Exception attendue avec message explicite

### Résultats Attendus

```
# Production Tests
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

# Delivery Tests
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

## 🧪 Testing avec Postman

### Collections Postman Disponibles

Les collections Postman complètes et testées sont disponibles dans le dossier `postman/` :

1. **Supply-Module.postman_collection.json** (72 requêtes)
   - Suppliers (Fournisseurs)
   - Raw Materials (Matières Premières)
   - Supply Orders (Commandes d'Approvisionnement)

2. **Production-Module.postman_collection.json** (95 requêtes)
   - Products (Produits)
   - Bills of Material (Nomenclatures)
   - Production Orders (Ordres de Production)

3. **Delivery-Module.postman_collection.json** (33 requêtes)
   - Customers (Clients)
   - Delivery Orders (Commandes de Livraison)
   - Deliveries (Livraisons physiques)

### Import dans Postman

1. Ouvrir Postman
2. Cliquer sur "Import"
3. Sélectionner les fichiers JSON dans le dossier `postman/`
4. Configurer la variable d'environnement `baseUrl` : `http://localhost:8081`

## 🔄 Workflow de Production Complet

### Exemple : Fabriquer 10 Chaises

```bash
# 1. Créer un produit
POST /api/production/products
{
  "code": "CHAIR-001",
  "name": "Chaise en bois",
  "description": "Chaise ergonomique",
  "category": "Mobilier",
  "productionTime": 120,
  "cost": 45.50,
  "stock": 0.0,
  "stockMin": 10.0
}

# 2. Créer une nomenclature (BOM) - Lier produit aux matières
POST /api/production/bills-of-material
{
  "productId": 1,
  "rawMaterialId": 1,  # Bois
  "quantity": 2.5,
  "unit": "kg"
}

# 3. Créer un ordre de production
POST /api/production/production-orders
{
  "orderNumber": "PO-2025-001",
  "productId": 1,
  "quantity": 10,
  "priority": "URGENT"
}
# Réponse: { "id": 1, "status": "EN_ATTENTE", ... }

# 4. Démarrer la production (vérifie les stocks de matières)
PATCH /api/production/production-orders/1/start
# Réponse: { "status": "EN_PRODUCTION", "startDate": "2025-11-02", ... }

# 5. Terminer la production (consomme matières, ajoute produits au stock)
PATCH /api/production/production-orders/1/complete
# Réponse: { 
#   "status": "TERMINE", 
#   "endDate": "2025-11-02",
#   "product": { "stock": 10.0 }  # Stock mis à jour !
# }
```

## 🚚 Workflow de Livraison Complet

### Exemple : Livrer 5 Chaises et 3 Tables à un Client

```bash
# 1. Créer un client
POST /api/delivery/customers
{
  "code": "CUST-001",
  "name": "ACME Corp",
  "contact": "Othman",
  "phone": "+212600000000",
  "email": "contact@acme.com",
  "address": "10 Rue Principale",
  "city": "Casablanca",
  "postalCode": "20000",
  "country": "Morocco"
}
# Réponse: { "id": 1, "code": "CUST-001", ... }

# 2. Créer une commande multi-produits (vérifie les stocks disponibles)
POST /api/delivery/orders
{
  "orderNumber": "ORD-2025-001",
  "customerId": 1,
  "orderDate": "2025-11-04",
  "expectedDeliveryDate": "2025-11-06",
  "deliveryAddress": "10 Rue Principale",
  "deliveryCity": "Casablanca",
  "deliveryPostalCode": "20000",
  "status": "EN_PREPARATION",
  "orderLines": [
    { "productId": 1, "quantity": 5, "unitPrice": 50.0 },  # 5 Chaises
    { "productId": 2, "quantity": 3, "unitPrice": 120.0 }  # 3 Tables
  ]
}
# Réponse: { 
#   "id": 1, 
#   "status": "EN_PREPARATION",
#   "totalAmount": 610.0,  # (5*50) + (3*120) = 610
#   "orderLines": [...]
# }

# 3. Créer une livraison physique
POST /api/delivery/deliveries
{
  "deliveryNumber": "DEL-2025-001",
  "deliveryOrderId": 1,
  "vehicle": "Truck-12",
  "driver": "Ahmed",
  "driverPhone": "+212600111222",
  "status": "PLANIFIEE",
  "deliveryDate": "2025-11-06",
  "cost": 75.0,
  "trackingNumber": "TRK-2025-001"
}
# Réponse: { "id": 1, "status": "PLANIFIEE", "trackingNumber": "TRK-2025-001", ... }

# 4. Changer le statut de la commande (En route)
PATCH /api/delivery/orders/1/status?status=EN_ROUTE
# Réponse: { "status": "EN_ROUTE", ... }

# 5. Démarrer la livraison
PATCH /api/delivery/deliveries/1/status?status=EN_COURS
# Réponse: { "status": "EN_COURS", ... }

# 6. Marquer comme livrée (met à jour automatiquement la date)
PATCH /api/delivery/deliveries/1/deliver
# Réponse: { 
#   "status": "LIVREE",
#   "actualDeliveryDate": "2025-11-04",
#   ...
# }

# 7. Finaliser la commande
PATCH /api/delivery/orders/1/status?status=LIVREE
# Réponse: { 
#   "status": "LIVREE",
#   "actualDeliveryDate": "2025-11-04",
#   ...
# }

# 8. Tracking - Le client peut suivre sa livraison
GET /api/delivery/deliveries/tracking/TRK-2025-001
# Réponse: { "status": "LIVREE", "driver": "Ahmed", "actualDeliveryDate": "2025-11-04", ... }
```

## 📊 Base de Données

### Tables Créées (Liquibase)

- **suppliers** - Fournisseurs
- **raw_materials** - Matières premières
- **raw_materials_suppliers** - Relation N-N matières/fournisseurs
- **supply_orders** - Commandes d'approvisionnement
- **supply_order_lines** - Lignes de commande
- **products** - Produits finis
- **bills_of_material** - Nomenclatures (recettes)
- **production_orders** - Ordres de production
- **customers** - Clients
- **delivery_orders** - Commandes de livraison
- **delivery_order_lines** - Lignes de commande
- **deliveries** - Livraisons physiques
- **audit_logs** - Journal d'audit
- **databasechangelog** - Migrations Liquibase

### Relations Principales

```
Supplier 1→N RawMaterial
RawMaterial N→N Supplier (via raw_materials_suppliers)
RawMaterial 1→N BillOfMaterial
Product 1→N BillOfMaterial
Product 1→N ProductionOrder
Product 1→N DeliveryOrderLine
SupplyOrder 1→N SupplyOrderLine
Customer 1→N DeliveryOrder
DeliveryOrder 1→N DeliveryOrderLine
DeliveryOrder 1→1 Delivery
```

## 🔐 Sécurité

- Validation Jakarta sur tous les DTOs
- Exception handling global
- Health check endpoint : `/actuator/health`

## 📈 Monitoring

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

Réponse attendue :
```json
{
  "status": "UP"
}
```

## 🐛 Troubleshooting

### Port déjà utilisé
```bash
# Trouver le processus sur le port 8081
lsof -i :8081
# Tuer le processus
kill -9 <PID>
```

### Erreur de connexion MySQL
- Vérifier que MySQL est démarré
- Vérifier les credentials dans `application.properties`
- Vérifier que la base de données existe

### Erreurs de compilation
```bash
# Nettoyer et recompiler
mvn clean install -DskipTests
```

### Tests échouent avec erreur de connexion DB
- Les tests utilisent H2 in-memory (profil `test` activé automatiquement)
- Si besoin, vérifier `supplychainx-app/src/test/resources/application-test.properties`
- Les tests ne nécessitent PAS MySQL en cours d'exécution

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📝 Changelog

### Version 1.0.0 (Novembre 2025)
- ✅ Module Supply complet (Suppliers, Raw Materials, Supply Orders)
- ✅ Module Production complet (Products, BOMs, Production Orders)
- ✅ Module Delivery complet (Customers, Delivery Orders, Deliveries)
- ✅ Workflow de production fonctionnel (avec consommation matières)
- ✅ Workflow de livraison fonctionnel (avec validation stocks produits)
- ✅ Collections Postman testées (200+ requêtes)
- ✅ Tests d'intégration (Production: 3 tests, Delivery: 7 tests)
- ✅ Documentation Swagger
- ✅ 128+ endpoints REST
- ✅ Gestion des exceptions unifiée (BusinessException, ResourceNotFoundException)

## 👨‍💻 Auteur

**Othman Aboussebaba**
- GitHub: [@Aboussebaba-Othman](https://github.com/Aboussebaba-Othman)




- Spring Boot
- MySQL
- MapStruct
- Liquibase

---

**Made with ❤️ for efficient supply chain management**
