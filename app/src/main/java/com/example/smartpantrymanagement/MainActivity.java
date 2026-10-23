package com.example.smartpantrymanagement;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PANTRY_DB";

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

        final int initialLeft = mainView.getPaddingLeft();
        final int initialTop = mainView.getPaddingTop();
        final int initialRight = mainView.getPaddingRight();
        final int initialBottom = mainView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                mainView, (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout());

                    view.setPadding(
                            initialLeft + systemBars.left,
                            initialTop + systemBars.top,
                            initialRight + systemBars.right,
                            initialBottom + systemBars.bottom);

                    return insets;
                });


        // Find the views from activity_main.xml.
        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmptyPantry = findViewById(R.id.textEmptyPantry);

        // Configure the RecyclerView.
        recyclerPantry.setLayoutManager(
                new LinearLayoutManager(this));

        pantryAdapter = new PantryAdapter();
        recyclerPantry.setAdapter(pantryAdapter);

        // Initialize the database.
        databaseHelper = new DatabaseHelper(this);

        // Load the saved ingredients.
        loadPantryItems();
    }

    private void loadPantryItems() {

        List<PantryItem> items = new ArrayList<>();

        try (Cursor cursor = databaseHelper.getAllPantryItems()) {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity"));

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit"));

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date"));

                PantryItem item = new PantryItem(
                        id, name, quantity, unit, expiryDate);

                items.add(item);
            }

        } catch (Exception e) {
            Log.e(TAG, "Could not load pantry items.", e);

            Toast.makeText(
                    this,
                    "Could not load pantry items.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Send the data to the adapter.
        pantryAdapter.setPantryItems(items);

        // Show a message when the pantry has no items.
        if (items.isEmpty()) {
            textEmptyPantry.setVisibility(View.VISIBLE);
            recyclerPantry.setVisibility(View.GONE);
        } else {
            textEmptyPantry.setVisibility(View.GONE);
            recyclerPantry.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "Pantry items displayed: " + items.size());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the list whenever the screen becomes active.
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
