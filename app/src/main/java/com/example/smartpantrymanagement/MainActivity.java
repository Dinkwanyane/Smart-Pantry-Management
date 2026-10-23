package com.example.smartpantrymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;
    private RecyclerView recyclerPantry;
    private TextView textEmptyPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    mainView,
                    (view, windowInsets) -> {
                        Insets systemBars = windowInsets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        view.setPadding(
                                systemBars.left + 16,
                                systemBars.top + 16,
                                systemBars.right + 16,
                                systemBars.bottom + 16
                        );

                        return windowInsets;
                    }
            );
        }

        Button buttonAddIngredient =
                findViewById(R.id.buttonAddIngredient);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);

        recyclerPantry.setLayoutManager(
                new LinearLayoutManager(this)
        );

        pantryAdapter = new PantryAdapter(
                this::openEditIngredient,
                this::showDeleteConfirmation
        );

        recyclerPantry.setAdapter(pantryAdapter);

        databaseHelper = new DatabaseHelper(this);

        buttonAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddEditIngredientActivity.class
            );

            startActivity(intent);
        });

        loadPantryItems();
    }

    private void openEditIngredient(PantryItem item) {
        Intent intent = new Intent(
                MainActivity.this,
                AddEditIngredientActivity.class
        );

        intent.putExtra("ITEM_ID", item.getId());

        startActivity(intent);
    }

    private void showDeleteConfirmation(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete \""
                                + item.getName()
                                + "\" from your pantry?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteIngredient(item);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void deleteIngredient(PantryItem item) {
        try {
            int result = databaseHelper.deletePantryItem(
                    item.getId()
            );

            if (result > 0) {
                Toast.makeText(
                        this,
                        "Ingredient deleted successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                loadPantryItems();
            } else {
                Toast.makeText(
                        this,
                        "Ingredient could not be deleted.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {
            Log.e(
                    "MainActivity",
                    "Error deleting ingredient",
                    e
            );

            Toast.makeText(
                    this,
                    "An error occurred while deleting the ingredient.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void loadPantryItems() {
        List<PantryItem> items = new ArrayList<>();

        try (Cursor cursor = databaseHelper.getAllPantryItems()) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date")
                );

                PantryItem item = new PantryItem(
                        id,
                        name,
                        quantity,
                        unit,
                        expiryDate
                );

                items.add(item);
            }

            pantryAdapter.setPantryItems(items);

            boolean isEmpty = items.isEmpty();

            textEmptyPantry.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE
            );

            recyclerPantry.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE
            );

        } catch (Exception e) {
            Log.e(
                    "MainActivity",
                    "Error loading pantry items",
                    e
            );

            Toast.makeText(
                    this,
                    "Could not load pantry ingredients.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && pantryAdapter != null) {
            loadPantryItems();
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
