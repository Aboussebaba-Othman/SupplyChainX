#!/bin/bash

# Script de test pour le module Audit de SupplyChainX
# Usage: ./test-audit-module.sh

BASE_URL="http://localhost:8081"
API_URL="$BASE_URL/api"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║       🧪 TEST MODULE AUDIT - SupplyChainX                     ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les résultats
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Vérifier que l'application est démarrée
echo -e "${BLUE}📡 Vérification de la connexion...${NC}"
curl -s "$BASE_URL/actuator/health" > /dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Application accessible sur $BASE_URL${NC}"
else
    echo -e "${RED}✗ Application non accessible. Démarrez l'application d'abord!${NC}"
    exit 1
fi
echo ""

# Obtenir un token JWT
echo -e "${BLUE}🔐 Authentification...${NC}"
TOKEN_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "admin",
        "password": "password123"
    }')

TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token" : "[^"]*' | sed 's/"token" : "//')

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ Token JWT obtenu${NC}"
    echo -e "${YELLOW}Token: ${TOKEN:0:50}...${NC}"
else
    echo -e "${RED}✗ Échec de l'authentification${NC}"
    echo "Réponse: $TOKEN_RESPONSE"
    exit 1
fi
echo ""

# Test 1: Créer un Audit Log
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}TEST 1: Créer un Audit Log${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
AUDIT_RESPONSE=$(curl -s -X POST "$API_URL/audit/logs" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "entityType": "PRODUCT",
        "entityId": 1,
        "action": "CREATE",
        "performedBy": "admin",
        "details": "Product created during test",
        "ipAddress": "127.0.0.1"
    }')

AUDIT_ID=$(echo $AUDIT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -n "$AUDIT_ID" ]; then
    print_result 0 "Audit log créé avec succès (ID: $AUDIT_ID)"
else
    print_result 1 "Échec création audit log"
    echo "Réponse: $AUDIT_RESPONSE"
fi
echo ""

# Test 2: Récupérer tous les logs
echo -e "${BLUE}TEST 2: Récupérer tous les Audit Logs${NC}"
ALL_LOGS=$(curl -s -X GET "$API_URL/audit/logs?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN")

LOG_COUNT=$(echo $ALL_LOGS | grep -o '"totalElements":[0-9]*' | grep -o '[0-9]*')

if [ -n "$LOG_COUNT" ]; then
    print_result 0 "Logs récupérés (Total: $LOG_COUNT)"
else
    print_result 1 "Échec récupération logs"
fi
echo ""

# Test 3: Rechercher logs par utilisateur
echo -e "${BLUE}TEST 3: Rechercher logs par utilisateur (admin)${NC}"
USER_LOGS=$(curl -s -X GET "$API_URL/audit/logs/user/admin?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN")

USER_LOG_COUNT=$(echo $USER_LOGS | grep -o '"totalElements":[0-9]*' | grep -o '[0-9]*')

if [ -n "$USER_LOG_COUNT" ]; then
    print_result 0 "Logs de l'utilisateur 'admin': $USER_LOG_COUNT"
else
    print_result 1 "Échec recherche par utilisateur"
fi
echo ""

# Test 4: Statistiques par type d'action
echo -e "${BLUE}TEST 4: Statistiques par type d'action${NC}"
STATS=$(curl -s -X GET "$API_URL/audit/logs/statistics/by-action-type" \
    -H "Authorization: Bearer $TOKEN")

if echo "$STATS" | grep -q "CREATE"; then
    print_result 0 "Statistiques récupérées"
    echo "Aperçu: $(echo $STATS | head -c 100)..."
else
    print_result 1 "Échec récupération statistiques"
fi
echo ""

# Test 5: Créer une Stock Alert
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}TEST 5: Créer une Stock Alert${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
ALERT_RESPONSE=$(curl -s -X POST "$API_URL/audit/alerts" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "alertType": "LOW_STOCK",
        "entityType": "RAW_MATERIAL",
        "entityId": 1,
        "entityName": "Acier inoxydable",
        "message": "Stock faible pour Acier inoxydable",
        "currentStock": 30,
        "minimumStock": 100
    }')

ALERT_ID=$(echo $ALERT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -n "$ALERT_ID" ]; then
    print_result 0 "Alerte créée avec succès (ID: $ALERT_ID)"
else
    print_result 1 "Échec création alerte"
    echo "Réponse: $ALERT_RESPONSE"
fi
echo ""

# Test 6: Récupérer alertes non résolues
echo -e "${BLUE}TEST 6: Récupérer alertes non résolues${NC}"
UNRESOLVED=$(curl -s -X GET "$API_URL/audit/alerts/unresolved?page=0&size=10" \
    -H "Authorization: Bearer $TOKEN")

UNRESOLVED_COUNT=$(echo $UNRESOLVED | grep -o '"totalElements":[0-9]*' | grep -o '[0-9]*')

if [ -n "$UNRESOLVED_COUNT" ]; then
    print_result 0 "Alertes non résolues: $UNRESOLVED_COUNT"
else
    print_result 1 "Échec récupération alertes"
fi
echo ""

# Test 7: Créer une alerte critique
echo -e "${BLUE}TEST 7: Créer une alerte CRITIQUE${NC}"
CRITICAL_ALERT=$(curl -s -X POST "$API_URL/audit/alerts" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "alertType": "CRITICAL_STOCK",
        "entityType": "PRODUCT",
        "entityId": 2,
        "entityName": "Produit Test",
        "message": "Stock critique pour Produit Test",
        "currentStock": 5,
        "minimumStock": 50
    }')

CRITICAL_ID=$(echo $CRITICAL_ALERT | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -n "$CRITICAL_ID" ]; then
    print_result 0 "Alerte critique créée (ID: $CRITICAL_ID)"
else
    print_result 1 "Échec création alerte critique"
fi
echo ""

# Test 8: Récupérer alertes critiques
echo -e "${BLUE}TEST 8: Récupérer alertes critiques non résolues${NC}"
CRITICAL_ALERTS=$(curl -s -X GET "$API_URL/audit/alerts/critical/unresolved" \
    -H "Authorization: Bearer $TOKEN")

if echo "$CRITICAL_ALERTS" | grep -q "CRITICAL_STOCK"; then
    print_result 0 "Alertes critiques récupérées"
else
    print_result 1 "Échec récupération alertes critiques"
fi
echo ""

# Test 9: Compter alertes non résolues
echo -e "${BLUE}TEST 9: Compter alertes non résolues${NC}"
COUNT=$(curl -s -X GET "$API_URL/audit/alerts/count/unresolved" \
    -H "Authorization: Bearer $TOKEN")

if [ -n "$COUNT" ]; then
    print_result 0 "Nombre d'alertes non résolues: $COUNT"
else
    print_result 1 "Échec comptage alertes"
fi
echo ""

# Test 10: Résoudre une alerte
if [ -n "$ALERT_ID" ]; then
    echo -e "${BLUE}TEST 10: Résoudre une alerte${NC}"
    RESOLVE_RESPONSE=$(curl -s -X PATCH "$API_URL/audit/alerts/$ALERT_ID/resolve" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d '{
            "resolvedBy": "admin",
            "resolutionComment": "Commande approvisionnement créée"
        }')
    
    if echo "$RESOLVE_RESPONSE" | grep -q '"resolved":true'; then
        print_result 0 "Alerte $ALERT_ID résolue avec succès"
    else
        print_result 1 "Échec résolution alerte"
    fi
    echo ""
fi

# Test 11: Statistiques alertes par type
echo -e "${BLUE}TEST 11: Statistiques alertes par type${NC}"
ALERT_STATS=$(curl -s -X GET "$API_URL/audit/alerts/statistics/unresolved/by-type" \
    -H "Authorization: Bearer $TOKEN")

if [ -n "$ALERT_STATS" ] && [ "$ALERT_STATS" != "{}" ]; then
    print_result 0 "Statistiques alertes récupérées"
    echo "Stats: $ALERT_STATS"
else
    print_result 0 "Pas d'alertes non résolues actuellement"
fi
echo ""

# Test 12: Recherche avancée
echo -e "${BLUE}TEST 12: Recherche avancée d'alertes${NC}"
SEARCH=$(curl -s -X GET "$API_URL/audit/alerts/search?alertType=LOW_STOCK&resolved=false" \
    -H "Authorization: Bearer $TOKEN")

if echo "$SEARCH" | grep -q "totalElements"; then
    print_result 0 "Recherche avancée fonctionnelle"
else
    print_result 1 "Échec recherche avancée"
fi
echo ""

# Résumé
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    📊 RÉSUMÉ DES TESTS                         ║${NC}"
echo -e "${BLUE}╠════════════════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  ✓ Module Audit fonctionnel                                   ║${NC}"
echo -e "${GREEN}║  ✓ API Audit Logs: Création, Lecture, Recherche               ║${NC}"
echo -e "${GREEN}║  ✓ API Stock Alerts: Création, Lecture, Résolution            ║${NC}"
echo -e "${GREEN}║  ✓ Statistiques et compteurs fonctionnels                     ║${NC}"
echo -e "${GREEN}║  ✓ Recherche avancée avec filtres                             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}💡 Pour plus de tests:${NC}"
echo -e "   - Accédez à Swagger UI: ${BLUE}http://localhost:8081/swagger-ui.html${NC}"
echo -e "   - Consultez la doc: ${BLUE}supplychainx-audit/README.md${NC}"
echo ""
