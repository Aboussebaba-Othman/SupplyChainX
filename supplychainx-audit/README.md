# Module Audit - SupplyChainX

## 📋 Description

Le module **supplychainx-audit** est responsable de la **traçabilité** et de la **surveillance** du système SupplyChainX. Il gère :

- **Logs d'audit** : Enregistrement de toutes les actions effectuées sur les entités du système
- **Alertes de stock** : Surveillance automatique des niveaux de stock (matières premières et produits finis)
- **Notifications par email** : Envoi automatique d'alertes critiques par SMTP
- **Tâches planifiées** : Vérifications périodiques des stocks et nettoyage des anciennes données

---

## 🏗️ Architecture

### Entités

#### AuditLog
```java
- entityType: EntityType         // Type d'entité concernée
- entityId: Long                 // ID de l'entité
- action: ActionType             // Type d'action (CREATE, UPDATE, DELETE, etc.)
- performedBy: String            // Utilisateur qui a effectué l'action
- timestamp: LocalDateTime       // Date et heure de l'action
- details: String                // Détails supplémentaires
- ipAddress: String              // Adresse IP
- oldValues: String              // Anciennes valeurs (JSON)
- newValues: String              // Nouvelles valeurs (JSON)
```

#### StockAlert
```java
- alertType: AlertType           // Type d'alerte (LOW_STOCK, CRITICAL_STOCK, OUT_OF_STOCK)
- entityType: EntityType         // RAW_MATERIAL ou PRODUCT
- entityId: Long                 // ID de l'entité
- entityName: String             // Nom pour affichage
- message: String                // Message d'alerte
- currentStock: Integer          // Stock actuel
- minimumStock: Integer          // Seuil minimum
- resolved: boolean              // Alerte résolue ?
- resolvedAt: LocalDateTime      // Date de résolution
- emailSent: boolean             // Email envoyé ?
```

### Enums

- **AlertType**: `LOW_STOCK`, `CRITICAL_STOCK`, `OUT_OF_STOCK`, `DELIVERY_DELAY`, `ORDER_BLOCKED`
- **EntityType**: `SUPPLIER`, `RAW_MATERIAL`, `SUPPLY_ORDER`, `PRODUCT`, `PRODUCTION_ORDER`, `CUSTOMER`, `ORDER`, `DELIVERY`, `USER`
- **ActionType**: `CREATE`, `UPDATE`, `DELETE`, `READ`, `STATUS_CHANGE`, `LOGIN`, `LOGOUT`, `ACCESS_DENIED`

---

## 🔌 API REST Endpoints

### Audit Logs

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/audit/logs` | Créer un log d'audit |
| GET | `/api/audit/logs` | Récupérer tous les logs (pagination) |
| GET | `/api/audit/logs/entity-type/{entityType}` | Logs par type d'entité |
| GET | `/api/audit/logs/entity/{entityType}/{entityId}` | Logs pour une entité spécifique |
| GET | `/api/audit/logs/action/{action}` | Logs par type d'action |
| GET | `/api/audit/logs/user/{username}` | Logs par utilisateur |
| GET | `/api/audit/logs/search` | Recherche avancée avec filtres |
| GET | `/api/audit/logs/statistics/by-user` | Statistiques par utilisateur |
| GET | `/api/audit/logs/statistics/by-action-type` | Statistiques par action |
| GET | `/api/audit/logs/statistics/by-entity-type` | Statistiques par type d'entité |

### Stock Alerts

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/audit/alerts` | Créer une alerte de stock |
| GET | `/api/audit/alerts/{id}` | Récupérer une alerte par ID |
| GET | `/api/audit/alerts` | Récupérer toutes les alertes (pagination) |
| GET | `/api/audit/alerts/unresolved` | Alertes non résolues |
| GET | `/api/audit/alerts/resolved` | Alertes résolues |
| GET | `/api/audit/alerts/type/{alertType}` | Alertes par type |
| GET | `/api/audit/alerts/critical/unresolved` | Alertes critiques non résolues |
| GET | `/api/audit/alerts/entity/{entityType}/{entityId}` | Alertes pour une entité |
| PATCH | `/api/audit/alerts/{id}/resolve` | Résoudre une alerte |
| GET | `/api/audit/alerts/search` | Recherche avancée avec filtres |
| GET | `/api/audit/alerts/statistics/unresolved/by-type` | Statistiques des alertes non résolues |
| GET | `/api/audit/alerts/count/unresolved` | Nombre d'alertes non résolues |
| GET | `/api/audit/alerts/count/critical/unresolved` | Nombre d'alertes critiques |

---

## ⏰ Tâches Planifiées (Schedulers)

### 1. Vérification des Stocks
- **Fréquence** : Toutes les 6 heures (00h, 06h, 12h, 18h)
- **Cron** : `0 0 6-18/6 * * *`
- **Action** : Vérifie les stocks de matières premières et produits finis, crée des alertes si nécessaire

### 2. Envoi d'Emails d'Alerte
- **Fréquence** : Toutes les 30 minutes
- **Cron** : `0 0/30 * * * *`
- **Action** : Envoie les emails pour les alertes qui n'ont pas encore été envoyées

### 3. Nettoyage des Anciennes Données
- **Fréquence** : Tous les dimanches à 2h du matin
- **Cron** : `0 0 2 * * SUN`
- **Action** : Supprime les alertes résolues de plus de 90 jours

---

## 📧 Configuration Email (SMTP)

### Configuration dans `application.properties`

```properties
# SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email Application
spring.mail.from=noreply@supplychainx.com
app.alert.email.to=admin@supplychainx.com
app.alert.email.enabled=true
```

### Pour Gmail
1. Activer l'authentification à deux facteurs
2. Générer un **mot de passe d'application** : https://myaccount.google.com/apppasswords
3. Utiliser ce mot de passe dans `spring.mail.password`

---

## 🚀 Utilisation

### 1. Lancer le module en standalone

```bash
mvn spring-boot:run -pl supplychainx-audit
```

Le module démarre sur le port **8085** et expose :
- **API REST** : http://localhost:8085/api/audit/...
- **Swagger UI** : http://localhost:8085/swagger-ui.html

### 2. Créer un log d'audit programmatiquement

```java
@Autowired
private AuditLogService auditLogService;

public void createProduct(ProductRequestDTO productDTO) {
    // ... créer le produit ...
    
    // Logger l'action
    auditLogService.logAction(
        EntityType.PRODUCT,
        savedProduct.getId(),
        ActionType.CREATE,
        "admin",
        "Product created: " + savedProduct.getName()
    );
}
```

### 3. Créer une alerte de stock

```java
@Autowired
private StockAlertService stockAlertService;

public void checkStockLevel(RawMaterial material) {
    if (material.getStock() < material.getStockMin()) {
        StockAlertRequestDTO alertDTO = StockAlertRequestDTO.builder()
            .alertType(AlertType.LOW_STOCK)
            .entityType(EntityType.RAW_MATERIAL)
            .entityId(material.getId())
            .entityName(material.getName())
            .currentStock(material.getStock())
            .minimumStock(material.getStockMin())
            .message("Stock faible pour " + material.getName())
            .build();
            
        stockAlertService.createAlert(alertDTO);
    }
}
```

### 4. Résoudre une alerte

```bash
PATCH /api/audit/alerts/123/resolve
{
  "resolvedBy": "admin",
  "resolutionComment": "Commande d'approvisionnement créée"
}
```

---

## 📊 Exemples de Requêtes

### Recherche de logs d'audit

```bash
GET /api/audit/logs/search?entityType=PRODUCT&action=CREATE&startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59&page=0&size=20
```

### Statistiques d'audit par utilisateur

```bash
GET /api/audit/logs/statistics/by-user

Response:
{
  "admin": 1250,
  "supply_manager": 456,
  "production_manager": 789
}
```

### Alertes critiques non résolues

```bash
GET /api/audit/alerts/critical/unresolved

Response:
[
  {
    "id": 1,
    "alertType": "OUT_OF_STOCK",
    "entityType": "RAW_MATERIAL",
    "entityName": "Acier inoxydable",
    "currentStock": 0,
    "minimumStock": 100,
    "critical": true,
    "resolved": false,
    "emailSent": true
  }
]
```

---

## 🗄️ Base de Données

Le module crée automatiquement 2 tables via **Liquibase** :

### Table `audit_logs`
- Stocke tous les logs d'audit du système
- Index sur : entity_type, entity_id, action, performed_by, timestamp

### Table `stock_alerts`
- Stocke toutes les alertes de stock
- Index sur : alert_type, entity_type, entity_id, resolved, email_sent

---

## 🔧 Prochaines Étapes

✅ **Complété** :
- Entités (AuditLog, StockAlert)
- Repositories avec méthodes de recherche avancées
- Services métier
- Controllers REST
- Scheduler pour tâches automatiques
- Configuration Email
- Migrations Liquibase

⏳ **À faire** :
- [ ] Aspect AOP pour audit automatique de toutes les entités
- [ ] Tests unitaires (JUnit 5 + Mockito)
- [ ] Tests d'intégration
- [ ] Intégration avec modules supply et production pour vérification automatique des stocks

---

## 📝 Notes

- Les logs d'audit sont conservés **1 an** par défaut
- Les alertes résolues sont conservées **3 mois**
- Les alertes critiques (OUT_OF_STOCK, CRITICAL_STOCK) sont marquées comme prioritaires
- Les emails sont envoyés uniquement si `app.alert.email.enabled=true`

---

## 🤝 Contribution

Ce module fait partie du projet **SupplyChainX** et suit les mêmes conventions de code et d'architecture.
