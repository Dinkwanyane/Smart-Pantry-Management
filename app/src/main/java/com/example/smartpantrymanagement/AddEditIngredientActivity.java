package com.example.smartpantrymanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editQuantity;
    private EditText editUnit;
    private EditText editExpiryDate;
    private Button buttonSave;

    private DatabaseHelper databaseHelper;
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editUnit = findViewById(R.id.editUnit);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        buttonSave = findViewById(R.id.buttonSave);

        databaseHelper = new DatabaseHelper(this);

        editExpiryDate.setFocusable(false);
        editExpiryDate.setClickable(true);
        editExpiryDate.setOnClickListener(v -> showDatePicker());

        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        if (itemId == -1) {
            setTitle("Add Ingredient");
            buttonSave.setText("Save Ingredient");
        } else {
            setTitle("Edit Ingredient");
            buttonSave.setText("Update Ingredient");
            loadIngredient();
        }

        buttonSave.setOnClickListener(v -> saveIngredient());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    editExpiryDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadIngredient() {
        PantryItem item = databaseHelper.getPantryItem(itemId);

        if (item == null) {
            Toast.makeText(
                    this,
                    "Ingredient could not be found.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        editName.setText(item.getName());
        editQuantity.setText(String.valueOf(item.getQuantity()));
        editUnit.setText(item.getUnit());

        if (item.getExpiryDate() != null) {
            editExpiryDate.setText(item.getExpiryDate());
        } else {
            editExpiryDate.setText("");
        }
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String quantityText =
                editQuantity.getText().toString().trim();
        String unit = editUnit.getText().toString().trim();
        String expiryDate =
                editExpiryDate.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Enter an ingredient name.");
            editName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            editQuantity.setError("Enter a quantity.");
            editQuantity.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            editUnit.setError("Enter a unit.");
            editUnit.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid number.");
            editQuantity.requestFocus();
            return;
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            editQuantity.setError(
                    "Quantity must be a number greater than zero."
            );
            editQuantity.requestFocus();
            return;
        }

        String savedExpiryDate =
                expiryDate.isEmpty() ? null : expiryDate;

        if (itemId == -1) {
            long result = databaseHelper.addPantryItem(
                    name, quantity, unit, savedExpiryDate
            );

            if (result != -1) {
                Toast.makeText(
                        this,
                        "Ingredient added successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } else {
                Toast.makeText(
                        this,
                        "Could not add the ingredient.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } else {
            int result = databaseHelper.updatePantryItem(
                    itemId, name, quantity, unit, savedExpiryDate
            );

            if (result > 0) {
                Toast.makeText(
                        this,
                        "Ingredient updated successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } else {
                Toast.makeText(
                        this,
                        "No changes were saved. The ingredient may no longer exist.",
                        Toast.LENGTH_LONG
                ).show();
            }
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
