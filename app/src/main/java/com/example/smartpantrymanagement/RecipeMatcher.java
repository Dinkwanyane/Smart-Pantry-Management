package com.example.smartpantrymanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class: do not create RecipeMatcher objects.
    }

    public static class Requirement {
        public final String name;
        public final double quantity;
        public final String unit;

        public Requirement(String name, double quantity, String unit) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingredient name cannot be empty.");
            }

            if (unit == null || unit.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingredient unit cannot be empty.");
            }

            if (!Double.isFinite(quantity) || quantity <= 0) {
                throw new IllegalArgumentException(
                        "Required quantity must be positive.");
            }

            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    private static String normaliseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String value = name.trim().toLowerCase(Locale.ROOT);

        switch (value) {
            case "tomatoes":
                return "tomato";
            case "potatoes":
                return "potato";
            case "eggs":
                return "egg";
            case "carrots":
                return "carrot";
            case "onions":
                return "onion";
            case "apples":
                return "apple";
            default:
                return value;
        }
    }

    private static String normaliseUnit(String unit) {
        if (unit == null) {
            return "";
        }

        return unit.trim().toLowerCase(Locale.ROOT);
    }

    private static String ingredientKey(String name, String unit) {
        return normaliseName(name) + "|" + normaliseUnit(unit);
    }

    public static boolean canMakeRecipe(
            List<PantryItem> pantry,
            List<Requirement> requirements) {

        // Missing data must never produce a positive match.
        if (pantry == null || requirements == null
                || requirements.isEmpty()) {
            return false;
        }

        // Store available quantities separately for each ingredient and unit.
        Map<String, Double> available = new HashMap<>();

        for (PantryItem item : pantry) {
            if (item == null || item.getName() == null
                    || item.getUnit() == null) {
                continue;
            }

            double quantity = item.getQuantity();

            if (!Double.isFinite(quantity) || quantity <= 0) {
                continue;
            }

            String key = ingredientKey(
                    item.getName(), item.getUnit());

            available.put(
                    key,
                    available.getOrDefault(key, 0.0) + quantity
            );
        }

        // Every recipe requirement must be satisfied.
        for (Requirement requirement : requirements) {
            if (requirement == null) {
                return false;
            }

            String key = ingredientKey(
                    requirement.name, requirement.unit);

            // Missing ingredient or incompatible unit.
            if (!available.containsKey(key)) {
                return false;
            }

            // Insufficient quantity.
            if (available.get(key) < requirement.quantity) {
                return false;
            }
        }

        // All requirements passed.
        return true;
    }
}
