package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout recipeListContainer;
    private TextView textNoRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = new DatabaseHelper(this);

        recipeListContainer =
                findViewById(R.id.recipeListContainer);

        textNoRecipes =
                findViewById(R.id.textNoRecipes);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh suggestions whenever this screen becomes visible.
        displaySuggestedRecipes();
    }

    private void displaySuggestedRecipes() {

        // Remove old recipe buttons before refreshing the list.
        recipeListContainer.removeAllViews();

        // Get recipes that satisfy all their ingredient requirements.
        List<Recipe> suggestedRecipes =
                databaseHelper.getSuggestedRecipes();

        if (suggestedRecipes == null || suggestedRecipes.isEmpty()) {

            textNoRecipes.setVisibility(View.VISIBLE);
            recipeListContainer.setVisibility(View.GONE);

            textNoRecipes.setText(
                    "No recipes match your pantry yet. "
                            + "Add more ingredients and try again."
            );

            return;
        }

        textNoRecipes.setVisibility(View.GONE);
        recipeListContainer.setVisibility(View.VISIBLE);

        for (Recipe recipe : suggestedRecipes) {

            Button recipeButton = new Button(this);

            recipeButton.setText(recipe.getName());
            recipeButton.setAllCaps(false);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.bottomMargin = 12;
            recipeButton.setLayoutParams(params);

            recipeButton.setOnClickListener(view -> {

                Intent intent = new Intent(
                        SuggestedRecipesActivity.this,
                        RecipeDetailActivity.class
                );

                intent.putExtra("RECIPE_ID", recipe.getId());

                startActivity(intent);
            });

            recipeListContainer.addView(recipeButton);
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
