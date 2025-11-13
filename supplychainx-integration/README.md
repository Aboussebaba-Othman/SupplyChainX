# 🧪 Module de Tests d'Intégration - SupplyChainX

Module dédié aux tests d'intégration E2E avec TestContainers.

## 📋 Structure

```
supplychainx-integration/
├── src/test/java/com/supplychainx/integration/
│   ├── config/          # Configuration des tests
│   │   ├── IntegrationTest.java  # Classe de base abstraite
│   │   └── IntegrationTestConfig.java
│   ├── workflow/        # Tests de workflows complets
│   │   ├── SupplyWorkflowIntegrationTest.java
│   │   ├── ProductionWorkflowIntegrationTest.java
│   │   └── DeliveryWorkflowIntegrationTest.java
│   └── security/        # Tests de sécurité et permissions
│       ├── AuthenticationIntegrationTest.java
│       └── AuthorizationIntegrationTest.java
└── src/test/resources/
    └── application-test.yml  # Configuration pour les tests
```

## 🚀 Technologies

- **TestContainers**: Conteneurs Docker pour MySQL
- **Spring Boot Test**: Framework de test
- **MockMvc**: Tests des contrôleurs REST
- **JUnit 5**: Framework de test

## 💡 Usage

### Classe de Base

Tous les tests d'intégration doivent étendre `IntegrationTest`:

```java
class MyIntegrationTest extends IntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldTestSomething() {
        // Test code...
    }
}
```

### Avantages

- ✅ Conteneur MySQL automatique (TestContainers)
- ✅ Configuration Spring Boot complète
- ✅ MockMvc auto-configuré
- ✅ Profil "test" activé
- ✅ Réutilisation du conteneur entre tests

## 📝 Types de Tests

### 1. Tests de Workflow (E2E)

Tests complets simulant des scénarios réels:
- Supply: Fournisseur → Matière → Commande → Réception
- Production: Produit → BOM → Ordre de production → Fabrication
- Delivery: Client → Commande → Livraison

### 2. Tests de Sécurité

- Tests d'authentification JWT
- Tests d'autorisation par rôle
- Tests de permissions granulaires

## 🔧 Commandes

```bash
# Exécuter tous les tests d'intégration
mvn test -pl supplychainx-integration

# Exécuter un test spécifique
mvn test -pl supplychainx-integration -Dtest=SupplyWorkflowIntegrationTest

# Avec logs détaillés
mvn test -pl supplychainx-integration -X
```

## 📊 Configuration

Les tests utilisent:
- **MySQL 8.0** via TestContainers
- **Base de données**: `test_supplychainx_db`
- **Liquibase**: Activé pour la migration
- **JWT Secret**: Clé de test dédiée

## ⚙️ Prérequis

- Docker installé et démarré
- Java 17+
- Maven 3.8+

## 📖 Documentation

- [TestContainers](https://www.testcontainers.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
