# Guide de Tests - Module Production

## 📋 Collection Postman : Production Module

Cette collection contient **46 requêtes** réparties en 3 catégories pour tester l'intégralité du module Production.

## 🚀 Ordre d'Exécution Recommandé

### **Phase 1 : Setup - Matières Premières (Module Supply)**
Avant de tester le module Production, assurez-vous d'avoir des matières premières en stock.

**Exemples de matières premières à créer** :
```json
// POST /api/supply/raw-materials
{
  "code": "RM-BOIS-001",
  "name": "Bois de chêne massif",
  "description": "Planches de chêne séché",
  "category": "Bois",
  "unit": "kg",
  "unitPrice": 15.50,
  "stock": 500,
  "stockMin": 100,
  "supplierId": 1
}

// POST /api/supply/raw-materials
{
  "code": "RM-VIS-001",
  "name": "Vis acier 4x40mm",
  "description": "Vis de fixation en acier inoxydable",
  "category": "Quincaillerie",
  "unit": "pcs",
  "unitPrice": 0.25,
  "stock": 10000,
  "stockMin": 2000,
  "supplierId": 1
}
```

### **Phase 2 : Création des Produits**

1. **Create Product - Chaise** (`PROD-001`)
   - Stock initial : 100 unités
   - Temps de production : 120 min
   - Coût : 45.50 €

2. **Create Product - Table** (`PROD-002`)
   - Stock initial : 50 unités
   - Temps de production : 180 min
   - Coût : 120.00 €

3. **Get All Products** - Vérifier que les produits sont créés
4. **Get Product by Code** - Tester la recherche par code
5. **Get Low Stock Products** - Doit être vide (stocks > stockMin)
6. **Calculate Total Inventory Value** - Valeur totale = (100×45.5) + (50×120) = 10 550 €

### **Phase 3 : Création des Nomenclatures (BOM)**

⚠️ **Important** : Les IDs `rawMaterialId` doivent correspondre aux IDs réels de vos matières premières en base.

1. **Create BOM - Chaise (Bois)**
   ```json
   {
     "productId": 1,        // PROD-001 (Chaise)
     "rawMaterialId": 1,    // Bois de chêne
     "quantity": 2.5,       // 2.5 kg par chaise
     "unit": "kg"
   }
   ```

2. **Create BOM - Chaise (Vis)**
   ```json
   {
     "productId": 1,        // PROD-001 (Chaise)
     "rawMaterialId": 2,    // Vis acier
     "quantity": 12.0,      // 12 vis par chaise
     "unit": "pcs"
   }
   ```

3. **Create BOM - Table (Bois)**
   ```json
   {
     "productId": 2,        // PROD-002 (Table)
     "rawMaterialId": 1,    // Bois de chêne
     "quantity": 8.0,       // 8 kg par table
     "unit": "kg"
   }
   ```

4. **Get BOMs by Product** - Vérifier les nomenclatures créées
5. **Get Total Raw Material Quantity for Product** - Total pour PROD-001 = 2.5 + 12.0 = 14.5

### **Phase 4 : Gestion des Ordres de Production**

#### **Scénario 1 : Ordre Standard (Workflow Complet)**

1. **Create Production Order** (`PO-2025-001`)
   ```json
   {
     "orderNumber": "PO-2025-001",
     "productId": 1,      // Chaise
     "quantity": 10,      // 10 chaises
     "priority": "STANDARD"
   }
   ```
   → ✅ Statut initial : `EN_ATTENTE`
   → ✅ Temps estimé : 10 × 120 = 1200 min

2. **Get Orders by Status - EN_ATTENTE**
   → Doit afficher `PO-2025-001`

3. **Start Production** (ID: 1)
   → ⚠️ Vérifie automatiquement la disponibilité des matières premières :
   - Bois requis : 10 × 2.5 = 25 kg (stock : 500 kg) ✅
   - Vis requises : 10 × 12 = 120 pcs (stock : 10000 pcs) ✅
   → ✅ Statut passe à `EN_PRODUCTION`
   → ✅ `startDate` = date du jour

4. **Get Orders by Status - EN_PRODUCTION**
   → Doit afficher `PO-2025-001`

5. **Complete Production** (ID: 1)
   → ✅ Consomme les matières premières :
   - Bois : 500 - 25 = 475 kg
   - Vis : 10000 - 120 = 9880 pcs
   → ✅ Ajoute les produits finis au stock :
   - Chaises : 100 + 10 = 110 unités
   → ✅ Statut passe à `TERMINE`
   → ✅ `endDate` = date du jour

6. **Get Product by ID** (ID: 1)
   → Vérifier que le stock de chaises = 110

#### **Scénario 2 : Ordre Urgent**

1. **Create Production Order - Urgent** (`PO-2025-002`)
   ```json
   {
     "orderNumber": "PO-2025-002",
     "productId": 2,      // Table
     "quantity": 5,
     "priority": "URGENT"
   }
   ```

2. **Start Production** puis **Cancel Production Order**
   → Tester l'annulation d'un ordre en cours

#### **Scénario 3 : Stock Insuffisant**

1. Créer un ordre de production avec une grande quantité (ex: 100 chaises)
2. **Start Production**
   → ❌ Doit échouer avec message : "Matières premières insuffisantes"

### **Phase 5 : Gestion du Stock**

1. **Reduce Stock** - Retirer 25 unités de chaises
   → Stock passe de 110 à 85

2. **Add Stock** - Ajouter 50 unités
   → Stock passe de 85 à 135

3. **Get Low Stock Products**
   → Modifier `stockMin` d'un produit pour le tester

### **Phase 6 : Recherches et Rapports**

1. **Search Products by Name** - Rechercher "Chaise"
2. **Get Products by Category** - Catégorie "Mobilier"
3. **Get Orders by Product** - Tous les ordres pour PROD-001
4. **Get Orders by Date Range** - Ordres de novembre 2025
5. **Get Delayed Orders** - Ordres en retard (si `endDate` < aujourd'hui)

## 📊 Validation des Résultats

### **Points de Contrôle Clés**

| Test | Attendu | Vérification |
|------|---------|--------------|
| Création produit | HTTP 201 | `id`, `code`, `lowStock: false` |
| Création BOM | HTTP 201 | `lineCost` = quantity × rawMaterial.unitPrice |
| Création ordre | Statut `EN_ATTENTE` | `estimatedTime` calculé |
| Démarrage production | Statut `EN_PRODUCTION` | `startDate` renseignée |
| Fin production | Statut `TERMINE` | Stock produit augmenté, matières réduites |
| Annulation | Statut `ANNULE` | Impossible si `TERMINE` |
| Stock insuffisant | HTTP 400 | Message "Matières premières insuffisantes" |

## 🔍 Requêtes SQL de Vérification

```sql
-- Vérifier les produits
SELECT * FROM products;

-- Vérifier les nomenclatures avec coûts
SELECT b.*, 
       p.name AS product_name,
       r.name AS raw_material_name,
       (b.quantity * r.unit_price) AS line_cost
FROM bills_of_material b
JOIN products p ON b.product_id = p.id
JOIN raw_materials r ON b.raw_material_id = r.id;

-- Vérifier les ordres de production
SELECT po.*,
       p.name AS product_name,
       (po.quantity * p.production_time) AS calculated_time
FROM production_orders po
JOIN products p ON po.product_id = p.id
ORDER BY po.created_at DESC;

-- Vérifier les stocks de matières premières
SELECT code, name, stock, stock_min, 
       CASE WHEN stock < stock_min THEN 'LOW' ELSE 'OK' END AS status
FROM raw_materials;
```

## ⚠️ Erreurs Communes et Solutions

| Erreur | Cause | Solution |
|--------|-------|----------|
| 400 - "Produit non trouvé" | `productId` invalide | Vérifier les IDs existants avec GET /products |
| 400 - "Matière première non trouvée" | `rawMaterialId` invalide | Créer la matière première d'abord (Supply) |
| 400 - "Matières premières insuffisantes" | Stock < requis | Augmenter le stock ou réduire la quantité |
| 400 - "Seuls les ordres en attente peuvent être démarrés" | Statut incorrect | Vérifier le statut actuel de l'ordre |
| 400 - "Un produit avec ce code existe déjà" | Code dupliqué | Utiliser un code unique |

## 🎯 Scénario de Test Complet (End-to-End)

```bash
# 1. Créer un produit
POST /api/production/products → ID: 1

# 2. Créer les matières premières (Supply)
POST /api/supply/raw-materials → ID: 1 (Bois)
POST /api/supply/raw-materials → ID: 2 (Vis)

# 3. Créer la nomenclature
POST /api/production/bills-of-material (Produit 1 → Matière 1)
POST /api/production/bills-of-material (Produit 1 → Matière 2)

# 4. Créer un ordre de production
POST /api/production/orders → ID: 1 (statut: EN_ATTENTE)

# 5. Démarrer la production
PATCH /api/production/orders/1/start → (statut: EN_PRODUCTION)

# 6. Terminer la production
PATCH /api/production/orders/1/complete → (statut: TERMINE)
  → Stock produit augmenté
  → Stock matières réduit

# 7. Vérifier les résultats
GET /api/production/products/1 → Stock mis à jour
GET /api/supply/raw-materials/1 → Stock réduit
GET /api/production/orders/1 → Statut TERMINE
```

## 📝 Variables Postman

Configurez ces variables dans votre environnement Postman :

- `baseUrl` : `http://localhost:8081`
- `productId` : ID du produit créé (pour réutilisation)
- `orderId` : ID de l'ordre créé
- `rawMaterialId` : ID de la matière première

## 🛠️ Commandes Utiles

```bash
# Démarrer l'application
mvn spring-boot:run -pl supplychainx-app

# Vérifier les tables créées
docker exec supplychainx-mysql mysql -usupplychainx_user -psupplychainx_password -e "USE supplychainx_db; SHOW TABLES;"

# Vider les tables (ATTENTION : perte de données)
docker exec supplychainx-mysql mysql -usupplychainx_user -psupplychainx_password -e "
USE supplychainx_db;
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE production_orders;
TRUNCATE TABLE bills_of_material;
TRUNCATE TABLE products;
SET FOREIGN_KEY_CHECKS=1;
"
```

---

## ✅ Checklist de Tests

- [ ] Tous les produits créés
- [ ] Toutes les nomenclatures créées
- [ ] Ordres de production créés avec différents statuts
- [ ] Workflow complet testé (EN_ATTENTE → EN_PRODUCTION → TERMINE)
- [ ] Vérification des stocks après production
- [ ] Tests des recherches (nom, catégorie, statut)
- [ ] Tests d'annulation
- [ ] Tests de stock insuffisant
- [ ] Tests de gestion du stock (add/reduce)
- [ ] Vérification des calculs (temps estimé, coût total, valeur inventaire)

**Bon testing ! 🚀**
