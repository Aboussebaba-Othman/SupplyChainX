# Guide de Tests - Module Supply

## 📋 Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Configuration initiale](#configuration-initiale)
3. [Scénarios de test](#scénarios-de-test)
4. [Tests par endpoint](#tests-par-endpoint)
5. [Validation des données](#validation-des-données)
6. [Requêtes SQL utiles](#requêtes-sql-utiles)

## 🎯 Vue d'ensemble

Cette collection Postman contient **72 requêtes** pour tester le module Supply :
- **Suppliers** : 12 endpoints (CRUD, recherche, filtres, validation)
- **Raw Materials** : 17 endpoints (CRUD, stock, inventaire, par fournisseur)
- **Supply Orders** : 20 endpoints (lifecycle complet, statistiques)
- **Supply Order Lines** : 11 endpoints (lignes de commande, calculs)

## ⚙️ Configuration initiale

### 1. Importer la collection
```bash
# Ouvrir Postman
# File > Import > Choisir "Supply-Module.postman_collection.json"
```

### 2. Vérifier la variable d'environnement
La collection utilise la variable `{{baseUrl}}` :
- Par défaut : `http://localhost:8081`
- Modifier si votre serveur utilise un autre port

### 3. Vérifier que l'application tourne
```bash
# Dans un terminal
curl http://localhost:8081/actuator/health
# Devrait retourner: {"status":"UP"}
```

## 🧪 Scénarios de test

### Scénario 1 : Configuration initiale des fournisseurs

**Objectif** : Créer les fournisseurs de base

```
1. Create Supplier - Bois & Cie
   POST /api/suppliers
   Body: {
     "code": "SUP-001",
     "name": "Bois & Cie",
     "rating": 4.5,
     "leadTimeDays": 7
   }
   ✓ Status: 201 Created
   ✓ Response contient: id, code="SUP-001", rating=4.5

2. Create Supplier - Quincaillerie Pro
   ✓ Status: 201 Created
   ✓ Response contient: id, code="SUP-002", rating=5.0

3. Get All Suppliers
   GET /api/suppliers?page=0&size=20
   ✓ Status: 200 OK
   ✓ Response.data.content.length >= 2

4. Get Top Rated Suppliers
   GET /api/suppliers/top-rated
   ✓ Status: 200 OK
   ✓ Vérifie que "Quincaillerie Pro" est en tête (rating=5.0)
```

**Validation SQL** :
```sql
SELECT code, name, rating, city 
FROM suppliers 
ORDER BY rating DESC, name;
```

---

### Scénario 2 : Gestion des matières premières

**Objectif** : Créer et gérer le catalogue de matières premières

```
1. Create Raw Material - Bois de Chêne
   POST /api/raw-materials
   Body: {
     "code": "RM-BOIS-001",
     "name": "Bois de chêne massif",
     "category": "Bois",
     "unit": "kg",
     "unitPrice": 15.50,
     "stock": 500,
     "stockMin": 100
   }
   ✓ Status: 201 Created
   ✓ code="RM-BOIS-001", category="Bois", stock=500

2. Create Raw Material - Vis Acier
   POST /api/raw-materials
   Body: {
     "code": "RM-VIS-001",
     "name": "Vis acier 4x40mm",
     "category": "Quincaillerie",
     "unit": "pièce",
     "unitPrice": 0.15,
     "stock": 10000,
     "stockMin": 2000
   }
   ✓ Status: 201 Created
   ✓ code="RM-VIS-001", category="Quincaillerie", stock=10000

3. Create Raw Material - Vernis
   POST /api/raw-materials
   Body: {
     "code": "RM-VERN-001",
     "name": "Vernis brillant",
     "category": "Finition",
     "unit": "litre",
     "unitPrice": 25.00,
     "stock": 50,
     "stockMin": 10
   }
   ✓ Status: 201 Created
   ✓ code="RM-VERN-001", category="Finition", stock=50

4. Get All Raw Materials
   GET /api/raw-materials
   ✓ Status: 200 OK
   ✓ Response.data.content.length = 3

5. Get Raw Materials by Category - Bois
   GET /api/raw-materials/category/Bois
   ✓ Status: 200 OK
   ✓ Tous les éléments ont category="Bois"

6. Get Low Stock Raw Materials
   GET /api/raw-materials/low-stock
   ✓ Status: 200 OK
   ✓ Liste les matières où stock <= stock_min
```

**Validation SQL** :
```sql
-- Vérifier le stock par catégorie
SELECT category, COUNT(*) as nb_materials, 
       SUM(stock) as total_stock,
       SUM(stock * unit_price) as total_value
FROM raw_materials
GROUP BY category;

-- Matières en stock faible
SELECT code, name, stock, stock_min
FROM raw_materials
WHERE stock <= stock_min;
```

---

### Scénario 3 : Cycle de vie d'une commande d'approvisionnement

**Objectif** : Tester le workflow complet d'une commande

```
1. Create Supply Order - Bois
   POST /api/supply-orders
   Body: {
     "orderNumber": "SO-2025-001",
     "supplierId": 1,
     "orderDate": "2025-11-01",
     "expectedDeliveryDate": "2025-11-10",
     "status": "EN_ATTENTE",
     "lines": []
   }
   ✓ Status: 201 Created
   ✓ Sauvegarder l'ID de la commande

2. Add Order Line - Bois de Chêne
   POST /api/supply-order-lines/order/1
   Body: {
     "rawMaterialId": 1,
     "quantity": 200,
     "unitPrice": 15.50
   }
   ✓ Status: 201 Created
   ✓ totalPrice = 200 × 15.50 = 3100

3. Get Order Total
   GET /api/supply-order-lines/order/1/total-amount
   ✓ Status: 200 OK
   ✓ Vérifie data = 3100.0

4. Update Order Status to EN_COURS
   PATCH /api/supply-orders/1/status?status=EN_COURS
   ✓ Status: 200 OK
   ✓ status = "EN_COURS"

5. Receive Supply Order
   PATCH /api/supply-orders/1/receive?actualDeliveryDate=2025-11-08
   ✓ Status: 200 OK
   ✓ status = "RECUE"
   ✓ Vérifier que le stock de la matière a augmenté de 200
```

**Validation SQL** :
```sql
-- État de la commande
SELECT 
    so.order_number,
    so.status,
    s.name as supplier_name,
    so.order_date,
    so.expected_date,
    COUNT(sol.id) as nb_lines,
    SUM(sol.quantity * sol.unit_price) as total
FROM supply_orders so
LEFT JOIN supply_order_lines sol ON so.id = sol.supply_order_id
JOIN suppliers s ON so.supplier_id = s.id
WHERE so.order_number = 'SO-2025-001'
GROUP BY so.id;

-- Vérifier le stock après réception
SELECT code, name, stock 
FROM raw_materials 
WHERE id = 1;
-- Stock devrait être: 500 + 200 = 700
```

---

### Scénario 4 : Gestion des stocks

**Objectif** : Tester les opérations de stock (ajout/réduction)

```
1. Get Raw Material by Code (RM-BOIS-001)
   GET /api/raw-materials/code/RM-BOIS-001
   ✓ Noter le stock initial (ex: 700 après réception)

2. Reduce Stock
   PATCH /api/raw-materials/1/reduce-stock?quantity=50
   ✓ Status: 200 OK
   ✓ Nouveau stock = 700 - 50 = 650

3. Add Stock
   PATCH /api/raw-materials/1/add-stock?quantity=100
   ✓ Status: 200 OK
   ✓ Nouveau stock = 650 + 100 = 750

4. Get Low Stock Raw Materials
   GET /api/raw-materials/low-stock
   ✓ Status: 200 OK
   ✓ Liste les matières où stock <= stock_min

5. Count Low Stock Raw Materials
   GET /api/raw-materials/low-stock/count
   ✓ Status: 200 OK
   ✓ Retourne le nombre de matières en alerte
```

**Validation SQL** :
```sql
-- Historique des mouvements de stock (depuis audit)
SELECT 
    code, name, stock, stock_min,
    CASE 
        WHEN stock <= stock_min THEN 'ALERTE'
        WHEN stock <= stock_min * 1.5 THEN 'ATTENTION'
        ELSE 'OK'
    END as status
FROM raw_materials
ORDER BY stock / NULLIF(stock_min, 1);
```

---

### Scénario 5 : Tests de recherche et filtres

**Objectif** : Valider tous les endpoints de recherche

```
# Suppliers
1. Search Suppliers by Name (name="Bois")
   GET /api/suppliers/search?name=Bois
   ✓ Retourne "Bois & Cie"

2. Get Suppliers by Min Rating
   GET /api/suppliers/rating/4.0
   ✓ Filtre par note minimale

3. Get Suppliers by Max Lead Time
   GET /api/suppliers/lead-time/7
   ✓ Filtre par délai maximal

# Raw Materials
4. Search Raw Materials by Name (name="Vis")
   GET /api/raw-materials/search?name=Vis
   ✓ Retourne "Vis acier 4x40mm"

5. Get Raw Materials by Category (category="Quincaillerie")
   GET /api/raw-materials/category/Quincaillerie
   ✓ Filtre par catégorie

6. Get Raw Materials by Supplier
   GET /api/raw-materials/supplier/1
   ✓ Liste les matières du fournisseur

# Supply Orders
7. Get Orders by Status (status="EN_ATTENTE")
   GET /api/supply-orders/status/EN_ATTENTE
   ✓ Filtre par statut

8. Get Orders by Supplier (supplierId=1)
   GET /api/supply-orders/supplier/1
   ✓ Filtre par fournisseur

9. Get Orders by Date Range
   GET /api/supply-orders/date-range?startDate=2025-11-01&endDate=2025-11-30
   ✓ Filtre par intervalle de dates

10. Get Delayed Orders
    GET /api/supply-orders/delayed
    ✓ Commandes dont expectedDeliveryDate < aujourd'hui et status = EN_COURS
```

---

## 🔍 Tests par endpoint

### Suppliers

| Endpoint | Méthode | Test à vérifier |
|----------|---------|-----------------|
| `/api/suppliers` | POST | Code unique, email valide, rating 0-5 |
| `/api/suppliers` | GET | Pagination, tri |
| `/api/suppliers/{id}` | GET | ID invalide → 404 |
| `/api/suppliers/code/{code}` | GET | Code inexistant → 404 |
| `/api/suppliers/search` | GET | Recherche insensible à la casse |
| `/api/suppliers/rating/{minRating}` | GET | Filtre >= minRating |
| `/api/suppliers/lead-time/{maxLeadTime}` | GET | Filtre <= maxLeadTime |
| `/api/suppliers/top-rated` | GET | Tri par rating DESC |
| `/api/suppliers/{id}/can-delete` | GET | Vérifie commandes actives |
| `/api/suppliers/{id}` | PUT | Mise à jour partielle possible |
| `/api/suppliers/{id}` | DELETE | Erreur si commandes actives |

### Raw Materials

| Endpoint | Méthode | Test à vérifier |
|----------|---------|-----------------|
| `/api/raw-materials` | POST | Code unique, stock >= 0, unitPrice > 0 |
| `/api/raw-materials/low-stock` | GET | Retourne stock <= stock_min |
| `/api/raw-materials/low-stock/paginated` | GET | Version paginée du low-stock |
| `/api/raw-materials/low-stock/count` | GET | Compte matières en alerte |
| `/api/raw-materials/supplier/{id}` | GET | Matières du fournisseur |
| `/api/raw-materials/{id}/add-stock` | PATCH | Stock incrémente correctement |
| `/api/raw-materials/{id}/reduce-stock` | PATCH | Stock ne peut pas être négatif |
| `/api/raw-materials/{id}/can-delete` | GET | Vérifie lignes de commande actives |

### Supply Orders

| Endpoint | Méthode | Test à vérifier |
|----------|---------|-----------------|
| `/api/supply-orders` | POST | orderNumber unique, dates cohérentes |
| `/api/supply-orders/number/{number}` | GET | Recherche par numéro |
| `/api/supply-orders/status/{status}` | GET | Filtre par statut |
| `/api/supply-orders/supplier/{id}` | GET | Commandes du fournisseur |
| `/api/supply-orders/supplier/{id}/status/{status}` | GET | Double filtre |
| `/api/supply-orders/date-range` | GET | Filtre entre deux dates |
| `/api/supply-orders/delayed` | GET | expectedDeliveryDate < today AND status = EN_COURS |
| `/api/supply-orders/recent` | GET | Dernières commandes créées |
| `/api/supply-orders/{id}/status` | PATCH | Validation transitions de statut |
| `/api/supply-orders/{id}/receive` | PATCH | Status → RECUE, stock mis à jour |
| `/api/supply-orders/{id}/cancel` | PATCH | Annulation possible si pas RECUE |
| `/api/supply-orders/supplier/{id}/active-count` | GET | Compte EN_ATTENTE + EN_COURS |
| `/api/supply-orders/total-amount/status/{status}` | GET | Somme montants par statut |
| `/api/supply-orders/{id}/can-delete` | GET | Vérifie status != RECUE |
| `/api/supply-orders/{id}/can-modify` | GET | Vérifie status != RECUE/ANNULEE |

### Supply Order Lines

| Endpoint | Méthode | Test à vérifier |
|----------|---------|-----------------|
| `/api/supply-order-lines/order/{id}` | POST | Ajout ligne à commande existante |
| `/api/supply-order-lines/{id}` | PUT | Mise à jour quantité/prix |
| `/api/supply-order-lines/{id}` | DELETE | Ne pas supprimer dernière ligne |
| `/api/supply-order-lines/order/{id}` | GET | Lignes d'une commande |
| `/api/supply-order-lines/material/{id}` | GET | Lignes contenant une matière |
| `/api/supply-order-lines/order/{id}/material/{id}` | GET | Ligne spécifique |
| `/api/supply-order-lines/material/{id}/total-quantity` | GET | Σ quantités dans commandes actives |
| `/api/supply-order-lines/order/{id}/total-amount` | GET | Σ(quantity × unitPrice) |
| `/api/supply-order-lines/material/{id}/has-active` | GET | Boolean si matière dans commandes actives |

---

## ✅ Validation des données

### Tests de validations métier

```javascript
// Test 1: Un supplier ne peut pas avoir rating > 5
POST /api/suppliers
Body: { ..., "rating": 6.0 }
Expected: 400 Bad Request

// Test 2: Stock ne peut pas être négatif
PATCH /api/raw-materials/1/reduce-stock?quantity=10000
Expected: 400 Bad Request (si stock actuel < 10000)

// Test 3: orderNumber doit être unique
POST /api/supply-orders
Body: { "orderNumber": "SO-2025-001", ... }
Expected: 409 Conflict (si SO-2025-001 existe déjà)

// Test 4: expectedDeliveryDate doit être >= orderDate
POST /api/supply-orders
Body: { 
  "orderDate": "2025-11-10",
  "expectedDeliveryDate": "2025-11-05"
}
Expected: 400 Bad Request

// Test 5: Can't delete supplier with active orders
DELETE /api/suppliers/1
Expected: 400 Bad Request (si commandes EN_ATTENTE ou EN_COURS)

// Test 6: Can't modify received order
PUT /api/supply-orders/1
Expected: 400 Bad Request (si status = RECUE)
```

---

## 📊 Requêtes SQL utiles

### Dashboard Supply

```sql
-- Vue d'ensemble
SELECT 
    (SELECT COUNT(*) FROM suppliers) as nb_suppliers,
    (SELECT COUNT(*) FROM raw_materials) as nb_materials,
    (SELECT COUNT(*) FROM supply_orders WHERE status != 'ANNULEE') as nb_active_orders,
    (SELECT COUNT(*) FROM raw_materials WHERE stock <= stock_min) as nb_low_stock,
    (SELECT SUM(stock * unit_price) FROM raw_materials) as inventory_value;
```

### Commandes par statut

```sql
SELECT 
    status,
    COUNT(*) as nb_orders,
    SUM(total_amount) as total_value
FROM supply_orders
GROUP BY status
ORDER BY status;
```

### Top fournisseurs

```sql
SELECT 
    s.code,
    s.name,
    s.rating,
    COUNT(so.id) as nb_orders,
    SUM(so.total_amount) as total_spent
FROM suppliers s
LEFT JOIN supply_orders so ON s.id = so.supplier_id
GROUP BY s.id
ORDER BY total_spent DESC, s.rating DESC
LIMIT 10;
```

### Matières les plus commandées

```sql
SELECT 
    rm.code,
    rm.name,
    rm.category,
    COUNT(sol.id) as nb_order_lines,
    SUM(sol.quantity) as total_ordered,
    SUM(sol.quantity * sol.unit_price) as total_value
FROM raw_materials rm
JOIN supply_order_lines sol ON rm.id = sol.raw_material_id
GROUP BY rm.id
ORDER BY total_value DESC
LIMIT 10;
```

### Délais de livraison moyens

```sql
SELECT 
    s.name as supplier_name,
    AVG(DATEDIFF(so.received_date, so.order_date)) as avg_lead_time_days,
    COUNT(*) as nb_received_orders
FROM supply_orders so
JOIN suppliers s ON so.supplier_id = s.id
WHERE so.status = 'RECUE' AND so.received_date IS NOT NULL
GROUP BY s.id
ORDER BY avg_lead_time_days;
```

### Commandes en retard

```sql
SELECT 
    so.order_number,
    s.name as supplier_name,
    so.order_date,
    so.expected_date,
    DATEDIFF(CURRENT_DATE, so.expected_date) as days_late,
    so.status
FROM supply_orders so
JOIN suppliers s ON so.supplier_id = s.id
WHERE so.expected_date < CURRENT_DATE
  AND so.status NOT IN ('RECUE', 'ANNULEE')
ORDER BY days_late DESC;
```

---

## 📝 Checklist de tests

### Phase 1: Configuration de base
- [ ] Créer 2-3 fournisseurs
- [ ] Créer 5-10 matières premières (diverses catégories)
- [ ] Créer les liens material_suppliers
- [ ] Vérifier les fournisseurs préférés

### Phase 2: Tests CRUD
- [ ] GET all (pagination, tri)
- [ ] GET by ID (valide et invalide)
- [ ] GET by code/name (recherche)
- [ ] POST (création valide)
- [ ] PUT (mise à jour)
- [ ] DELETE (avec et sans cascade)

### Phase 3: Workflow commandes
- [ ] Créer commande (EN_ATTENTE)
- [ ] Ajouter lignes de commande
- [ ] Calculer total
- [ ] Confirmer commande (CONFIRMEE)
- [ ] Recevoir commande (RECUE)
- [ ] Vérifier mise à jour stock
- [ ] Annuler une commande

### Phase 4: Tests de stock
- [ ] Add stock (quantité positive)
- [ ] Reduce stock (quantité positive)
- [ ] Low stock alert
- [ ] Calcul valeur inventaire

### Phase 5: Tests de recherche
- [ ] Recherche par nom
- [ ] Filtrage par catégorie
- [ ] Filtrage par ville
- [ ] Filtrage par date
- [ ] Filtrage par statut

### Phase 6: Tests d'erreur
- [ ] ID inexistant → 404
- [ ] Code dupliqué → 409
- [ ] Données invalides → 400
- [ ] Stock négatif → 400
- [ ] Dates incohérentes → 400

---

## 🎓 Exemples de workflows complets

### Workflow 1: Approvisionnement complet

```
1. Créer fournisseur "Bois & Cie"
   POST /api/suppliers

2. Créer matière "Bois de chêne" (stock initial: 100)
   POST /api/raw-materials

3. Créer commande d'approvisionnement (SO-001)
   POST /api/supply-orders

4. Ajouter ligne: 200 kg de bois à 15.50€
   POST /api/supply-order-lines/order/1

5. Mettre à jour statut à EN_COURS
   PATCH /api/supply-orders/1/status?status=EN_COURS

6. Recevoir la commande
   PATCH /api/supply-orders/1/receive?actualDeliveryDate=2025-11-08

7. Vérifier stock final = 100 + 200 = 300
   GET /api/raw-materials/code/RM-BOIS-001
```

### Workflow 2: Détection stock faible

```
1. Créer matière avec stock=50, stock_min=100
   POST /api/raw-materials

2. GET /api/raw-materials/low-stock → doit apparaître

3. Créer commande pour réapprovisionner
   POST /api/supply-orders + POST /api/supply-order-lines/order/{id}

4. Recevoir commande (stock=150)
   PATCH /api/supply-orders/{id}/receive

5. GET /api/raw-materials/low-stock → ne doit plus apparaître
```

### Workflow 3: Annulation de commande

```
1. Créer commande (status=EN_ATTENTE)
   POST /api/supply-orders

2. Ajouter lignes
   POST /api/supply-order-lines/order/{id}

3. Annuler (PATCH /cancel)
   PATCH /api/supply-orders/{id}/cancel

4. Vérifier status=ANNULEE
   GET /api/supply-orders/{id}

5. Tenter de modifier → doit échouer
   PUT /api/supply-orders/{id}
```

---

## 🚀 Prochaines étapes

Après avoir validé tous les tests:
1. ✅ Vérifier l'intégration avec le module Production (BOM)
2. ✅ Tester les transactions (rollback en cas d'erreur)
3. ✅ Tester les performances (temps de réponse < 100ms)
4. ✅ Vérifier les logs d'audit
5. ✅ Merge vers `dev`

---

**Astuce**: Utilisez les tests Postman automatiques en ajoutant des scripts dans l'onglet "Tests" de chaque requête:

```javascript
// Exemple de test automatique
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has id", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
});
```

Bonne chance avec vos tests! 🎉
