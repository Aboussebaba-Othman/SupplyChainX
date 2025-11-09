# 📦 Collections Postman - SupplyChainX

Ce dossier contient toutes les collections Postman pour tester l'application SupplyChainX.

---

## 📁 Organisation des Collections

### **🎯 Collection Principale (Avec Permissions)**

| Fichier | Description | Requêtes | Statut |
|---------|-------------|----------|--------|
| **SupplyChainX_Postman_Collection.json** | Collection complète avec gestion des permissions et JWT | 60+ | ✅ **Recommandée** |

**Caractéristiques** :
- ✅ Gestion automatique des tokens JWT (8 rôles)
- ✅ Tests de sécurité (401/403)
- ✅ Organisation par module (Supply, Production, Delivery)
- ✅ Tests positifs (✅) et négatifs (❌)
- ✅ Documentation complète des permissions

**Documentation** : Voir `../GUIDE_POSTMAN_COLLECTION.md`

---

### **📦 Collections par Module (Anciennes)**

| Fichier | Description | Statut |
|---------|-------------|--------|
| `Supply-Module.postman_collection.json` | Tests du module Supply uniquement | ⚠️ Obsolète |
| `Production-Module.postman_collection.json` | Tests du module Production uniquement | ⚠️ Obsolète |
| `Delivery-Module.postman_collection.json` | Tests du module Delivery uniquement | ⚠️ Obsolète |

**Note** : Ces collections sont conservées pour référence mais **la collection principale est recommandée**.

---

## 🚀 Utilisation Rapide

### **1. Importer la Collection Principale**

```bash
# Dans Postman
Import → postman/SupplyChainX_Postman_Collection.json
```

### **2. Configuration**

La collection utilise des **variables de collection** qui sont automatiquement configurées :

```javascript
base_url = http://localhost:8081
admin_token = (auto-généré après login)
gestionnaire_token = (auto-généré après login)
responsable_achats_token = (auto-généré après login)
// ... 8 tokens au total
```

### **3. Tester**

```
1. Dossier "🔐 Authentication"
   → Exécuter les logins (tokens auto-sauvegardés)

2. Dossier "📦 Supply Chain Module"
   → Tester avec différents tokens

3. Vérifier les tests de sécurité
   → Dossier "🧪 Tests de Sécurité"
```

---

## 📊 Structure de la Collection Principale

```
📂 SupplyChainX - Permission Testing Collection
├── 🔐 Authentication (7 requêtes)
│   ├── Login - ADMIN
│   ├── Login - GESTIONNAIRE_APPROVISIONNEMENT
│   ├── Login - RESPONSABLE_ACHATS
│   ├── Login - SUPERVISEUR_LOGISTIQUE
│   ├── Login - CHEF_PRODUCTION
│   ├── Get Current User
│   └── Refresh Token
│
├── 📦 Supply Chain Module (15 requêtes)
│   ├── 👥 Suppliers (7 requêtes)
│   ├── 🛒 Purchase Orders (5 requêtes)
│   └── 📦 Raw Materials (3 requêtes)
│
├── 🏭 Production Module (7 requêtes)
│   ├── 📦 Products (3 requêtes)
│   └── 🏭 Production Orders (4 requêtes)
│
├── 🚚 Delivery Module (7 requêtes)
│   ├── 👤 Customers (2 requêtes)
│   ├── 📋 Delivery Orders (2 requêtes)
│   └── 🚛 Deliveries (3 requêtes)
│
└── 🧪 Tests de Sécurité (4 requêtes)
    ├── ❌ Unauthorized (Sans Token)
    ├── ❌ Invalid Token
    ├── ❌ Forbidden (Wrong Permission)
    └── ❌ Cross-Module Access
```

---

## 🎯 Cas d'Usage

### **Cas 1 : Tester un Nouveau Endpoint**

1. Ajouter la requête dans le bon dossier
2. Utiliser `{{base_url}}` pour l'URL
3. Utiliser `{{admin_token}}` ou autre token selon le rôle
4. Préfixer avec ✅ ou ❌ selon le résultat attendu

### **Cas 2 : Tester les Permissions**

1. Créer plusieurs copies de la requête
2. Utiliser différents tokens (gestionnaire, superviseur, etc.)
3. Vérifier les codes de réponse (200, 403, 401)

### **Cas 3 : Tests Automatisés**

1. Utiliser le **Collection Runner** de Postman
2. Sélectionner toute la collection ou un dossier
3. Exécuter en batch
4. Analyser les résultats

---

## 🔑 Rôles et Permissions

| Rôle | Token Variable | Permissions |
|------|---------------|-------------|
| ADMIN | `{{admin_token}}` | Toutes les permissions |
| GESTIONNAIRE_APPROVISIONNEMENT | `{{gestionnaire_token}}` | Full CRUD supply + raw materials |
| RESPONSABLE_ACHATS | `{{responsable_achats_token}}` | SUPPLIER_READ, PURCHASE_ORDER_* |
| SUPERVISEUR_LOGISTIQUE | `{{superviseur_logistique_token}}` | Read-only supply |
| CHEF_PRODUCTION | `{{chef_production_token}}` | Full CRUD production |
| PLANIFICATEUR | `{{planificateur_token}}` | Production planning |
| SUPERVISEUR_PRODUCTION | `{{superviseur_production_token}}` | Production monitoring |
| GESTIONNAIRE_COMMERCIAL | `{{gestionnaire_commercial_token}}` | Customer & delivery orders |

**Détails complets** : Voir `../README_PERMISSIONS.md`

---

## 📝 Notes Importantes

### **Tokens JWT**

Les tokens sont **automatiquement sauvegardés** après chaque login grâce aux scripts de test :

```javascript
// Script automatique dans chaque Login
pm.test("Save token", function() {
    var response = pm.response.json();
    pm.collectionVariables.set('admin_token', response.token);
});
```

### **Base URL**

Par défaut : `http://localhost:8081`

Pour modifier :
```
1. Ouvrir la collection
2. Variables → base_url
3. Changer la valeur
```

### **Ordre d'Exécution**

**Important** : Exécuter d'abord les logins avant les autres requêtes !

```
1. 🔐 Authentication (pour générer les tokens)
2. 📦 Supply / 🏭 Production / 🚚 Delivery (tests fonctionnels)
3. 🧪 Tests de Sécurité (vérification des rejets)
```

---

## 🐛 Dépannage

### **Problème : 401 Unauthorized**

**Cause** : Token manquant ou expiré

**Solution** :
```
1. Aller dans "🔐 Authentication"
2. Exécuter le login du rôle concerné
3. Réessayer la requête
```

### **Problème : 403 Forbidden**

**Cause** : L'utilisateur n'a pas la permission requise

**Solution** : Normal si c'est un test négatif (❌), sinon vérifier le rôle utilisé

### **Problème : 404 Not Found**

**Cause** : L'application n'est pas démarrée ou mauvaise URL

**Solution** :
```bash
# Démarrer l'application
mvn spring-boot:run -pl supplychainx-app

# Vérifier que l'app écoute sur le port 8081
curl http://localhost:8081/actuator/health
```

---

## 📖 Documentation Complète

| Document | Description |
|----------|-------------|
| **GUIDE_POSTMAN_COLLECTION.md** | Guide détaillé d'utilisation |
| **GUIDE_TESTER_PERMISSIONS.md** | 3 approches de test (Manuel, Unitaire, Intégration) |
| **README_PERMISSIONS.md** | Architecture complète du système de permissions |
| **RECAP_POSTMAN_COLLECTION.md** | Récapitulatif visuel |

---

## 🎉 Résumé

✅ **Collection Principale** : `SupplyChainX_Postman_Collection.json` (60+ requêtes)  
✅ **Auto-configuration** : Tokens sauvegardés automatiquement  
✅ **Organisation** : Par module + tests de sécurité  
✅ **Documentation** : Complète et à jour  

**Prêt à tester !** 🚀
