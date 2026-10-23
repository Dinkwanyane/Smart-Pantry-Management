package com.example.smartpantrymanagement;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView recipeNameTextView;
    private TextView instructionsTextView;
    private TextView noIngredientsTextView;
    private LinearLayout ingredientsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        instructionsTextView = findViewById(R.id.instructionsTextView);
        noIngredientsTextView = findViewById(R.id.noIngredientsTextView);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);

        if (recipeId == -1) {
            Toast.makeText(
                    this,
                    "No valid recipe was selected.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        loadRecipeDetails(recipeId);
    }

    private void loadRecipeDetails(int recipeId) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String recipeName = null;
        String instructions = null;

        // Retrieve the selected recipe from SQLite.
        try (Cursor recipeCursor = db.query(
                "recipes",
                new String[]{"id", "name", "steps"},
                "id = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        )) {
            if (recipeCursor.moveToFirst()) {
                recipeName = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow("name")
                );

                instructions = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow("steps")
                );
            }
        }

        if (recipeName == null) {
            Toast.makeText(
                    this,
                    "Recipe not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        recipeNameTextView.setText(recipeName);
        instructionsTextView.setText(
                instructions == null || instructions.trim().isEmpty()
                        ? "No preparation instructions are available."
                        : instructions
        );

        // Retrieve every ingredient belonging to this recipe.
        ingredientsContainer.removeAllViews();

        int ingredientCount = 0;

        try (Cursor ingredientCursor = db.query(
                "recipe_ingredients",
                new String[]{"name", "quantity", "unit"},
                "recipe_id = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                "id ASC"
        )) {
            while (ingredientCursor.moveToNext()) {
                String ingredientName = ingredientCursor.getString(
                        ingredientCursor.getColumnIndexOrThrow("name")
                );

                double quantity = ingredientCursor.getDouble(
                        ingredientCursor.getColumnIndexOrThrow("quantity")
                );

                String unit = ingredientCursor.getString(
                        ingredientCursor.getColumnIndexOrThrow("unit")
                );

                TextView ingredientTextView = new TextView(this);

                ingredientTextView.setText(
                        "\u2022 " + ingredientName + " — "
                                + formatQuantity(quantity) + " "
                                + (unit == null ? "" : unit)
                );

                ingredientTextView.setTextSize(16);
                ingredientTextView.setPadding(0, 8, 0, 8);

                ingredientsContainer.addView(ingredientTextView);
                ingredientCount++;
            }
        }

        noIngredientsTextView.setVisibility(
                ingredientCount == 0 ? View.VISIBLE : View.GONE
        );

        db.close();
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)
                && Math.abs(quantity) < 1_000_000_000) {
            return String.format(
                    Locale.getDefault(),
                    "%.0f",
                    quantity
            );
        }

        return String.format(Locale.getDefault(), "%s", quantity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
