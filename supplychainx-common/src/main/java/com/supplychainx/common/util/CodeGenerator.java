package com.supplychainx.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitaire pour la génération automatique de codes et numéros séquentiels.
 *
 * Format des codes générés:
 * - Supplier: SUP-0001, SUP-0002, ...
 * - RawMaterial: MAT-0001, MAT-0002, ...
 * - Product: PROD-0001, PROD-0002, ...
 * - Customer: CLI-0001, CLI-0002, ...
 * - ProductionOrder: PO-2026-0001, PO-2026-0002, ...
 * - DeliveryOrder: DO-2026-0001, DO-2026-0002, ...
 * - Delivery: LIV-2026-0001, LIV-2026-0002, ...
 * - SupplyOrder: SO-2026-0001, SO-2026-0002, ...
 */
public final class CodeGenerator {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    // Préfixes pour chaque type d'entité
    public static final String PREFIX_SUPPLIER = "SUP";
    public static final String PREFIX_RAW_MATERIAL = "MAT";
    public static final String PREFIX_PRODUCT = "PROD";
    public static final String PREFIX_CUSTOMER = "CLI";
    public static final String PREFIX_PRODUCTION_ORDER = "PO";
    public static final String PREFIX_DELIVERY_ORDER = "DO";
    public static final String PREFIX_DELIVERY = "LIV";
    public static final String PREFIX_SUPPLY_ORDER = "SO";

    private CodeGenerator() {
        // Utility class - empêcher l'instanciation
    }

    /**
     * Génère un code simple sans année (pour entités de référence).
     * Format: PREFIX-0001
     *
     * @param prefix le préfixe du code
     * @param lastCode le dernier code utilisé (peut être null)
     * @return le nouveau code généré
     */
    public static String generateSimpleCode(String prefix, String lastCode) {
        int nextNumber = 1;

        if (lastCode != null && !lastCode.isEmpty()) {
            nextNumber = extractNumber(lastCode) + 1;
        }

        return String.format("%s-%04d", prefix, nextNumber);
    }

    /**
     * Génère un code avec année (pour commandes et livraisons).
     * Format: PREFIX-YYYY-0001
     *
     * @param prefix le préfixe du code
     * @param lastCode le dernier code utilisé (peut être null)
     * @return le nouveau code généré
     */
    public static String generateYearlyCode(String prefix, String lastCode) {
        String currentYear = LocalDate.now().format(YEAR_FORMATTER);
        int nextNumber = 1;

        if (lastCode != null && !lastCode.isEmpty()) {
            // Vérifier si le dernier code est de l'année en cours
            if (lastCode.contains(currentYear)) {
                nextNumber = extractNumber(lastCode) + 1;
            }
            // Sinon, on repart à 1 pour la nouvelle année
        }

        return String.format("%s-%s-%04d", prefix, currentYear, nextNumber);
    }

    /**
     * Extrait le numéro séquentiel d'un code.
     * Fonctionne avec les formats PREFIX-0001 et PREFIX-YYYY-0001
     *
     * @param code le code à analyser
     * @return le numéro extrait, ou 0 si non trouvé
     */
    public static int extractNumber(String code) {
        if (code == null || code.isEmpty()) {
            return 0;
        }

        // Pattern pour extraire le dernier groupe de chiffres
        Pattern pattern = Pattern.compile("(\\d+)$");
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    // Méthodes de commodité pour chaque type d'entité

    public static String generateSupplierCode(String lastCode) {
        return generateSimpleCode(PREFIX_SUPPLIER, lastCode);
    }

    public static String generateRawMaterialCode(String lastCode) {
        return generateSimpleCode(PREFIX_RAW_MATERIAL, lastCode);
    }

    public static String generateProductCode(String lastCode) {
        return generateSimpleCode(PREFIX_PRODUCT, lastCode);
    }

    public static String generateCustomerCode(String lastCode) {
        return generateSimpleCode(PREFIX_CUSTOMER, lastCode);
    }

    public static String generateProductionOrderNumber(String lastNumber) {
        return generateYearlyCode(PREFIX_PRODUCTION_ORDER, lastNumber);
    }

    public static String generateDeliveryOrderNumber(String lastNumber) {
        return generateYearlyCode(PREFIX_DELIVERY_ORDER, lastNumber);
    }

    public static String generateDeliveryNumber(String lastNumber) {
        return generateYearlyCode(PREFIX_DELIVERY, lastNumber);
    }

    public static String generateSupplyOrderNumber(String lastNumber) {
        return generateYearlyCode(PREFIX_SUPPLY_ORDER, lastNumber);
    }
}
