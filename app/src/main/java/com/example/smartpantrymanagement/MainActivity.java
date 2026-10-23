
package com.example.smartpantrymanagement;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PANTRY_DB";

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply system bar insets.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom);

                    return insets;
                });

        // Initialize the database helper ONCE.
        databaseHelper = new DatabaseHelper(this);

        try {
            // Open or create the SQLite database.
            SQLiteDatabase db =
                    databaseHelper.getWritableDatabase();

            Log.d(TAG, "Database opened successfully.");
            Log.d(TAG, "Database location: " + db.getPath());

            // Check saved record counts.
            int recipeCount = databaseHelper.getRecipeCount();
            int requirementCount =
                    databaseHelper.getRecipeIngredientCount();
            int pantryCount = databaseHelper.getPantryItemCount();

            Log.d(TAG, "Recipes: " + recipeCount);
            Log.d(TAG, "Ingredient requirements: "
                    + requirementCount);
            Log.d(TAG, "Pantry items: " + pantryCount);

            Toast.makeText(
                    this,
                    "Recipes: " + recipeCount
                            + "\nRequirements: " + requirementCount,
                    Toast.LENGTH_LONG
            ).show();

            // Display every recipe and its required ingredients.
            try (Cursor recipes = databaseHelper.getAllRecipes()) {

                while (recipes.moveToNext()) {

                    int recipeId = recipes.getInt(
                            recipes.getColumnIndexOrThrow("id"));

                    String recipeName = recipes.getString(
                            recipes.getColumnIndexOrThrow("name"));

                    Log.d(TAG, "Recipe: " + recipeName
                            + ", ID: " + recipeId);

                    try (Cursor ingredients =
                                 databaseHelper.getIngredientsForRecipe(
                                         recipeId)) {

                        while (ingredients.moveToNext()) {

                            String name = ingredients.getString(
                                    ingredients.getColumnIndexOrThrow(
                                            "name"));

                            double quantity = ingredients.getDouble(
                                    ingredients.getColumnIndexOrThrow(
                                            "quantity"));

                            String unit = ingredients.getString(
                                    ingredients.getColumnIndexOrThrow(
                                            "unit"));

                            Log.d(TAG, "  Required: " + name
                                    + ", " + quantity + " " + unit);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize or read the database.", e);

            Toast.makeText(
                    this,
                    "Database error. Check Logcat.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
