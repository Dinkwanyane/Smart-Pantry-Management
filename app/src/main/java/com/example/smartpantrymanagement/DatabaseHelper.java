
package com.example.smartpantrymanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";

    // Pantry table columns
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY_DATE = "expiry_date";

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

        // Create pantry items table
        db.execSQL(
                "CREATE TABLE " + TABLE_PANTRY + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_QUANTITY + " REAL NOT NULL CHECK (" +
                        COL_QUANTITY + " > 0), " +
                        COL_UNIT + " TEXT NOT NULL, " +
                        COL_EXPIRY_DATE + " TEXT)"
        );

        // Create recipes table
        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL UNIQUE, " +
                        "steps TEXT NOT NULL)"
        );

        // Create recipe ingredients table
        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recipe_id INTEGER NOT NULL, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_QUANTITY + " REAL NOT NULL CHECK (" +
                        COL_QUANTITY + " > 0), " +
                        COL_UNIT + " TEXT NOT NULL, " +
                        "FOREIGN KEY(recipe_id) REFERENCES " +
                        TABLE_RECIPES + "(" + COL_ID + ") " +
                        "ON DELETE CASCADE)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db, int oldVersion, int newVersion) {

        // Add database migration logic when the schema changes.
        // Do not drop existing tables here because that would
        // delete the user's stored pantry items and recipes.
    }

    // Validate pantry item input
    private boolean isValidPantryItem(
            String name, double quantity, String unit) {

        return name != null
                && !name.trim().isEmpty()
                && unit != null
                && !unit.trim().isEmpty()
                && quantity > 0
                && !Double.isNaN(quantity)
                && !Double.isInfinite(quantity);
    }

    // CREATE: Add a pantry item
    // Returns the new row ID, or -1 if insertion fails.
    public long addPantryItem(
            String name, double quantity,
            String unit, String expiryDate) {

        if (!isValidPantryItem(name, quantity, unit)) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit.trim());

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull(COL_EXPIRY_DATE);
        } else {
            values.put(COL_EXPIRY_DATE, expiryDate.trim());
        }

        return db.insert(TABLE_PANTRY, null, values);
    }

    // READ: Retrieve all pantry items, sorted by name
    public Cursor getAllPantryItems() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                COL_NAME + " ASC"
        );
    }

    // READ: Retrieve a pantry item by ID
    public Cursor getPantryItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
    }

    // UPDATE: Modify an existing pantry item
    // Returns the number of rows updated, or -1 for invalid input.
    public int updatePantryItem(
            int id, String name, double quantity,
            String unit, String expiryDate) {

        if (id <= 0 || !isValidPantryItem(name, quantity, unit)) {
            return -1;
        }

        ContentValues values = new ContentValues();

        values.put(COL_NAME, name.trim());
        values.put(COL_QUANTITY, quantity);
        values.put(COL_UNIT, unit.trim());

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull(COL_EXPIRY_DATE);
        } else {
            values.put(COL_EXPIRY_DATE, expiryDate.trim());
        }

        return getWritableDatabase().update(
                TABLE_PANTRY,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // DELETE: Remove a pantry item by ID
    // Returns the number of rows deleted.
    public int deletePantryItem(int id) {
        if (id <= 0) {
            return 0;
        }

        return getWritableDatabase().delete(
                TABLE_PANTRY,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Close any Cursor returned by this helper in the calling code.
}
