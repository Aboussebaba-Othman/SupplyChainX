# 🧪 Guide de Tests - SupplyChainX

Guide rapide pour tester l'application SupplyChainX avec Postman.

---

## 🚀 Démarrage Rapide

### 1. Lancer l'application
```bash
cd /home/othman/IdeaProjects/SupplyChainX
mvn spring-boot:run
```

### 2. Importer la collection Postman
```
File → Import → SupplyChainX_Postman_Collection.json
```

### 3. Se connecter (générer les tokens)
```
Dossier "🔐 Authentication" → Exécuter tous les logins
Les tokens sont automatiquement sauvegardés!
```

---

## 📋 Scénarios de Test Essentiels

### ✅ Scénario 1: Configuration Initiale des Fournisseurs

**Objectif**: Créer 2-3 fournisseurs de base

```
1. POST /api/suppliers - "Bois & Cie"
   ✓ Code: SUP-001, Rating: 4.5, LeadTime: 7 jours

2. POST /api/suppliers - "Quincaillerie Pro"  
   ✓ Code: SUP-002, Rating: 5.0, LeadTime: 3 jours

3. GET /api/suppliers
   ✓ Vérifie que les 2 fournisseurs sont présents

4. GET /api/suppliers/top-rated
   ✓ "Quincaillerie Pro" doit être en tête
```

---

### ✅ Scénario 2: Catalogue de Matières Premières

**Objectif**: Créer les matières premières nécessaires

```
1. POST /api/raw-materials - "Bois de chêne"
   ✓ Code: RM-BOIS-001, Category: Bois, Stock: 500, Min: 100

2. POST /api/raw-materials - "Vis acier"
   ✓ Code: RM-VIS-001, Category: Quincaillerie, Stock: 10000, Min: 2000

3. POST /api/raw-materials - "Vernis brillant"
   ✓ Code: RM-VERN-001, Category: Finition, Stock: 50, Min: 10

4. GET /api/raw-materials/low-stock
   ✓ Vérifie les matières en stock faible

5. GET /api/raw-materials/category/Bois
   ✓ Filtre par catégorie
```

---

### ✅ Scénario 3: Cycle Complet d'une Commande d'Approvisionnement

**Objectif**: Workflow complet d'une commande (EN_ATTENTE → RECUE)

```
1. POST /api/supply-orders
   ✓ OrderNumber: SO-2025-001, Status: EN_ATTENTE

2. POST /api/supply-order-lines/order/1
   ✓ MaterialId: 1, Quantity: 200, UnitPrice: 15.50

3. GET /api/supply-order-lines/order/1/total-amount
   ✓ Vérifie total = 3100.0

4. PATCH /api/supply-orders/1/status?status=EN_COURS
   ✓ Status passe à EN_COURS

5. PATCH /api/supply-orders/1/receive?actualDeliveryDate=2025-11-08
   ✓ Status → RECUE, Stock matière augmente de 200

6. GET /api/raw-materials/code/RM-BOIS-001
   ✓ Vérifie augmentation du stock
```

---

### ✅ Scénario 4: Gestion des Produits et Nomenclatures (BOM)

**Objectif**: Créer produits et leurs nomenclatures

```
1. POST /api/production/products - "Chaise en bois"
   ✓ Code: PROD-001, ProductionTime: 120min, Cost: 45.50€

2. POST /api/production/products - "Table rectangulaire"
   ✓ Code: PROD-002, ProductionTime: 180min, Cost: 120.00€

3. POST /api/production/bills-of-material
   ✓ ProductId: 1 (Chaise), RawMaterialId: 1 (Bois), Quantity: 2.5 kg

4. POST /api/production/bills-of-material
   ✓ ProductId: 1 (Chaise), RawMaterialId: 2 (Vis), Quantity: 12 pcs

5. GET /api/production/bills-of-material/product/1
   ✓ Liste les BOM de la chaise
```

---

### ✅ Scénario 5: Ordre de Production Complet

**Objectif**: Workflow production (EN_ATTENTE → TERMINEE)

```
1. POST /api/production/production-orders
   ✓ OrderNumber: PO-2025-001, ProductId: 1, Quantity: 10

2. GET /api/production/production-orders/order-number/PO-2025-001
   ✓ Status: EN_ATTENTE

3. PATCH /api/production/production-orders/1/start
   ✓ Status → EN_PRODUCTION
   ✓ Stock matières réduit

4. PATCH /api/production/production-orders/1/complete
   ✓ Status → TERMINEE
   ✓ Stock produit augmente de 10
```

---

### ✅ Scénario 6: Gestion des Clients et Livraisons

**Objectif**: Cycle complet commande client → livraison

```
1. POST /api/delivery/customers - "ACME Corp"
   ✓ Code: CUST-001, City: Casablanca

2. POST /api/delivery/orders
   ✓ OrderNumber: DO-2025-001, CustomerId: 1

3. POST /api/delivery/deliveries
   ✓ DeliveryNumber: DEL-2025-001, OrderId: 1

4. PATCH /api/delivery/deliveries/1/status?status=EN_COURS
   ✓ Status → EN_COURS

5. PATCH /api/delivery/deliveries/1/deliver
   ✓ Status → LIVREE
   ✓ Stock produit réduit
```

---

### ✅ Scénario 7: Tests de Sécurité (Permissions)

**Objectif**: Vérifier que les permissions fonctionnent

```
1. GET /api/suppliers (Sans token)
   ✗ Attendu: 401 Unauthorized

2. POST /api/suppliers (Token superviseur_logistique)
   ✗ Attendu: 403 Forbidden (read-only role)

3. POST /api/suppliers (Token gestionnaire_approvisionnement)
   ✓ Attendu: 201 Created
```

---

## 🔍 Tests de Recherche et Filtres

### Fournisseurs
- GET /api/suppliers/search?name=Bois
- GET /api/suppliers/rating/4.0
- GET /api/suppliers/top-rated

### Matières Premières
- GET /api/raw-materials/search?name=Vis
- GET /api/raw-materials/category/Quincaillerie
- GET /api/raw-materials/low-stock

### Commandes
- GET /api/supply-orders/status/EN_ATTENTE
- GET /api/supply-orders/supplier/1
- GET /api/supply-orders/delayed

### Produits
- GET /api/production/products/search?name=Chaise
- GET /api/production/products/low-stock

---

## ✅ Checklist de Tests

### Configuration
- [ ] Application démarrée sur port 8081
- [ ] Collection Postman importée
- [ ] Tokens JWT générés (8 rôles)

### Module Supply
- [ ] Créer 3 fournisseurs
- [ ] Créer 5+ matières premières
- [ ] Workflow commande: EN_ATTENTE → RECUE
- [ ] Vérifier mise à jour du stock

### Module Production
- [ ] Créer 2+ produits
- [ ] Créer nomenclatures (BOM)
- [ ] Workflow production: EN_ATTENTE → TERMINEE
- [ ] Vérifier stock produit/matières

### Module Delivery
- [ ] Créer 2+ clients
- [ ] Workflow livraison: PLANIFIEE → LIVREE
- [ ] Vérifier réduction stock produit

### Sécurité
- [ ] Test sans token (401)
- [ ] Test permission manquante (403)

---

## 🐛 Troubleshooting

### 401 Unauthorized
```
Cause: Token manquant ou expiré
Solution: Ré-exécuter le login correspondant
```

### 403 Forbidden
```
Cause: Permission manquante
Solution: Utiliser un token avec la bonne permission
```

### 404 Not Found
```
Cause: Application non démarrée
Solution: mvn spring-boot:run
```

---

**Prêt à tester!** 🚀

Commencer par les scénarios 1-3 pour la configuration de base, puis enchaîner sur les workflows complets.
