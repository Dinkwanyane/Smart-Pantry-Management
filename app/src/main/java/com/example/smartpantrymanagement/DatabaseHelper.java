package com.example.smartpantrymanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL CHECK(quantity > 0), " +
                        "unit TEXT NOT NULL, " +
                        "expiry_date TEXT)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "steps TEXT NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "quantity REAL NOT NULL CHECK(quantity > 0), " +
                        "unit TEXT NOT NULL, " +
                        "FOREIGN KEY(recipe_id) REFERENCES " +
                        TABLE_RECIPES + "(id) ON DELETE CASCADE, " +
                        "UNIQUE(recipe_id, name))"
        );

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            seedRecipes(db);
        }
    }

    // =========================================================
    // PANTRY: CREATE
    // =========================================================

    public long addPantryItem(
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        if (name == null || name.trim().isEmpty()
                || unit == null || unit.trim().isEmpty()
                || !Double.isFinite(quantity) || quantity <= 0) {
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());
        values.put("expiry_date", expiryDate);

        return getWritableDatabase().insert(
                TABLE_PANTRY, null, values);
    }

    // =========================================================
    // PANTRY: READ
    // =========================================================

    // Retained for existing screens that use a Cursor.
    public Cursor getAllPantryItems() {
        return getReadableDatabase().query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        );
    }

    public PantryItem getPantryItem(int id) {

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PANTRY,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null)) {

            if (cursor.moveToFirst()) {
                return pantryItemFromCursor(cursor);
            }
        }

        return null;
    }

    private PantryItem pantryItemFromCursor(Cursor cursor) {

        return new PantryItem(
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date"))
        );
    }

    // Convert pantry database rows into PantryItem objects.
    private List<PantryItem> loadPantryItems() {

        List<PantryItem> pantry = new ArrayList<>();

        try (Cursor cursor = getAllPantryItems()) {
            while (cursor.moveToNext()) {
                pantry.add(pantryItemFromCursor(cursor));
            }
        }

        return pantry;
    }

    // =========================================================
    // PANTRY: UPDATE
    // =========================================================

    public int updatePantryItem(
            int id,
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        if (name == null || name.trim().isEmpty()
                || unit == null || unit.trim().isEmpty()
                || !Double.isFinite(quantity) || quantity <= 0) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("quantity", quantity);
        values.put("unit", unit.trim());
        values.put("expiry_date", expiryDate);

        return getWritableDatabase().update(
                TABLE_PANTRY,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // =========================================================
    // PANTRY: DELETE
    // =========================================================

    public int deletePantryItem(int id) {
        return getWritableDatabase().delete(
                TABLE_PANTRY,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // =========================================================
    // RECIPES: READ
    // =========================================================

    // Retained for existing screens that use a Cursor.
    public Cursor getAllRecipes() {
        return getReadableDatabase().query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        );
    }

    public Cursor getIngredientsForRecipe(int recipeId) {
        return getReadableDatabase().query(
                TABLE_RECIPE_INGREDIENTS,
                null,
                "recipe_id = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                "name COLLATE NOCASE ASC"
        );
    }

    private Recipe recipeFromCursor(Cursor cursor) {

        return new Recipe(
                cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("name")),
                cursor.getString(
                        cursor.getColumnIndexOrThrow("steps"))
        );
    }

    private List<Recipe> loadRecipes() {

        List<Recipe> recipes = new ArrayList<>();

        try (Cursor cursor = getAllRecipes()) {
            while (cursor.moveToNext()) {
                recipes.add(recipeFromCursor(cursor));
            }
        }

        return recipes;
    }

    // =========================================================
    // COMMIT 9: STRICT RECIPE MATCHING
    // =========================================================

    public List<Recipe> getSuggestedRecipes() {

        List<Recipe> suggestedRecipes = new ArrayList<>();

        // Step 1: Read pantry rows and convert them into objects.
        List<PantryItem> pantry = loadPantryItems();

        // Step 2: Read all saved recipes.
        List<Recipe> recipes = loadRecipes();

        SQLiteDatabase db = getReadableDatabase();

        // Step 3: Check every recipe.
        for (Recipe recipe : recipes) {

            List<RecipeMatcher.Requirement> requirements =
                    new ArrayList<>();

            // Load the ingredients belonging to this recipe only.
            try (Cursor cursor = db.query(
                    TABLE_RECIPE_INGREDIENTS,
                    new String[]{"name", "quantity", "unit"},
                    "recipe_id = ?",
                    new String[]{String.valueOf(recipe.getId())},
                    null,
                    null,
                    "name COLLATE NOCASE ASC")) {

                while (cursor.moveToNext()) {

                    String name = cursor.getString(
                            cursor.getColumnIndexOrThrow("name"));

                    double quantity = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("quantity"));

                    String unit = cursor.getString(
                            cursor.getColumnIndexOrThrow("unit"));

                    requirements.add(
                            new RecipeMatcher.Requirement(
                                    name, quantity, unit));
                }
            }

            // Never suggest a recipe with no requirements.
            if (requirements.isEmpty()) {
                continue;
            }

            // Step 4: Add only complete matches.
            if (RecipeMatcher.canMakeRecipe(
                    pantry, requirements)) {

                suggestedRecipes.add(recipe);
            }
        }

        // Step 5: Return complete matches only.
        return suggestedRecipes;
    }

    // =========================================================
    // DATABASE COUNTS
    // =========================================================

    public int getRecipeCount() {
        return getCount(TABLE_RECIPES);
    }

    public int getRecipeIngredientCount() {
        return getCount(TABLE_RECIPE_INGREDIENTS);
    }

    public int getPantryItemCount() {
        return getCount(TABLE_PANTRY);
    }

    private int getCount(String tableName) {

        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (Cursor cursor =
                     getReadableDatabase().rawQuery(sql, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    // =========================================================
    // INITIAL RECIPE DATA
    // =========================================================

    private void seedRecipes(SQLiteDatabase db) {

        String[][] recipes = {
                {"Tomato Omelette",
                        "Beat eggs, chop tomato, and cook together."},
                {"Cheese Omelette",
                        "Beat eggs with cheese and cook in a pan."},
                {"Garlic Pasta",
                        "Cook pasta and mix with fried garlic and oil."},
                {"Tomato Pasta",
                        "Cook pasta and mix with tomato and garlic sauce."},
                {"Potato Mash",
                        "Boil potatoes and mash with milk and butter."},
                {"Vegetable Soup",
                        "Chop vegetables, simmer until soft, and season."},
                {"Carrot Salad",
                        "Grate carrots and mix with lemon juice and oil."},
                {"Egg Fried Rice",
                        "Scramble eggs and stir-fry with cooked rice."},
                {"Garlic Rice",
                        "Fry garlic in oil and stir in cooked rice."},
                {"Cheese Toast",
                        "Put cheese on bread and toast until melted."},
                {"Banana Pancakes",
                        "Mash banana, mix with egg and flour, then fry."},
                {"Apple Oatmeal",
                        "Cook oats in milk and add chopped apple."},
                {"Potato Wedges",
                        "Cut potatoes, coat with oil, season, and bake."},
                {"Onion Omelette",
                        "Mix chopped onion with beaten eggs and cook."},
                {"Carrot Soup",
                        "Boil carrots and onion, then blend until smooth."},
                {"Tomato Sandwich",
                        "Place sliced tomato and butter on bread."},
                {"Cheese Pasta",
                        "Cook 100g pasta and stir in 30.0g of cheese and 50.0ml of milk."},
                {"Apple Pancakes",
                        "Mix chopped apple into pancake batter and cook."},
                {"Garlic Toast",
                        "Spread garlic butter on bread and toast."},
                {"Vegetable Rice",
                        "Cook rice and mix with fried carrot and onion."}
        };

        // Each row: recipe index, ingredient, quantity, unit.
        Object[][] ingredients = {
                {0, "Eggs", 2.0, "piece"},
                {0, "Tomato", 1.0, "piece"},
                {1, "Eggs", 2.0, "piece"},
                {1, "Cheese", 30.0, "g"},
                {2, "Pasta", 100.0, "g"},
                {2, "Garlic", 2.0, "clove"},
                {2, "Oil", 1.0, "tbsp"},
                {3, "Pasta", 100.0, "g"},
                {3, "Tomato", 2.0, "piece"},
                {3, "Garlic", 1.0, "clove"},
                {4, "Potato", 2.0, "piece"},
                {4, "Milk", 50.0, "ml"},
                {4, "Butter", 10.0, "g"},
                {5, "Carrot", 2.0, "piece"},
                {5, "Potato", 1.0, "piece"},
                {5, "Onion", 1.0, "piece"},
                {6, "Carrot", 2.0, "piece"},
                {6, "Lemon", 1.0, "piece"},
                {6, "Oil", 1.0, "tbsp"},
                {7, "Eggs", 2.0, "piece"},
                {7, "Rice", 200.0, "g"},
                {7, "Oil", 1.0, "tbsp"},
                {8, "Rice", 200.0, "g"},
                {8, "Garlic", 2.0, "clove"},
                {8, "Oil", 1.0, "tbsp"},
                {9, "Bread", 2.0, "slice"},
                {9, "Cheese", 30.0, "g"},
                {10, "Banana", 1.0, "piece"},
                {10, "Eggs", 1.0, "piece"},
                {10, "Flour", 50.0, "g"},
                {11, "Oats", 50.0, "g"},
                {11, "Milk", 200.0, "ml"},
                {11, "Apple", 1.0, "piece"},
                {12, "Potato", 2.0, "piece"},
                {12, "Oil", 1.0, "tbsp"},
                {12, "Salt", 1.0, "pinch"},
                {13, "Eggs", 2.0, "piece"},
                {13, "Onion", 1.0, "piece"},
                {14, "Carrot", 3.0, "piece"},
                {14, "Onion", 1.0, "piece"},
                {14, "Water", 500.0, "ml"},
                {15, "Bread", 2.0, "slice"},
                {15, "Tomato", 1.0, "piece"},
                {15, "Butter", 10.0, "g"},
                {16, "Pasta", 100.0, "g"},
                {16, "Cheese", 40.0, "g"},
                {16, "Milk", 50.0, "ml"},
                {17, "Apple", 1.0, "piece"},
                {17, "Flour", 50.0, "g"},
                {17, "Milk", 100.0, "ml"},
                {18, "Bread", 2.0, "slice"},
                {18, "Garlic", 1.0, "clove"},
                {18, "Butter", 15.0, "g"},
                {19, "Rice", 200.0, "g"},
                {19, "Carrot", 1.0, "piece"},
                {19, "Onion", 1.0, "piece"}
        };


        db.beginTransaction();

        try {
            // Insert missing recipes without duplicating existing ones.
            for (String[] recipe : recipes) {
                ContentValues values = new ContentValues();
                values.put("name", recipe[0]);
                values.put("steps", recipe[1]);

                db.insertWithOnConflict(
                        TABLE_RECIPES,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            // Find each recipe's real database ID and insert its ingredients.
            for (Object[] item : ingredients) {
                int recipeIndex = (Integer) item[0];
                String recipeName = recipes[recipeIndex][0];
                String ingredientName = (String) item[1];
                double quantity = (Double) item[2];
                String unit = (String) item[3];

                long recipeId = -1;

                try (Cursor cursor = db.query(
                        TABLE_RECIPES,
                        new String[]{"id"},
                        "name = ?",
                        new String[]{recipeName},
                        null,
                        null,
                        null)) {

                    if (cursor.moveToFirst()) {
                        recipeId = cursor.getLong(0);
                    }
                }

                if (recipeId == -1) {
                    throw new IllegalStateException(
                            "Recipe not found: " + recipeName);
                }

                ContentValues values = new ContentValues();
                values.put("recipe_id", recipeId);
                values.put("name", ingredientName);
                values.put("quantity", quantity);
                values.put("unit", unit);

                db.insertWithOnConflict(
                        TABLE_RECIPE_INGREDIENTS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

    }
}