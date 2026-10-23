package com.example.smartpantrymanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editQuantity;
    private EditText editUnit;
    private EditText editExpiryDate;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Connect Java variables to the XML input fields.
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editUnit = findViewById(R.id.editUnit);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        buttonSave = findViewById(R.id.buttonSave);

        // Open the date picker when the expiry field is tapped.
        editExpiryDate.setFocusable(false);
        editExpiryDate.setOnClickListener(v -> showDatePicker());

        // Validate and save when the user taps Save.
        buttonSave.setOnClickListener(v -> saveIngredient());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(
                            java.util.Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    editExpiryDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void saveIngredient() {
        String name = editName.getText().toString().trim();
        String quantityText =
                editQuantity.getText().toString().trim();
        String unit = editUnit.getText().toString().trim();
        String expiryDate =
                editExpiryDate.getText().toString().trim();

        // 1. Validate required fields.
        if (name.isEmpty()
                || quantityText.isEmpty()
                || unit.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please complete all required fields",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 2. Convert quantity to a decimal number.
        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            editQuantity.setError("Enter a valid quantity");
            editQuantity.requestFocus();
            return;
        }

        // 3. Reject invalid quantities.
        if (!Double.isFinite(quantity) || quantity <= 0) {
            editQuantity.setError(
                    "Quantity must be greater than zero"
            );
            editQuantity.requestFocus();
            return;
        }

        // 4. Insert the validated ingredient into SQLite.
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        long result = databaseHelper.addPantryItem(
                name,
                quantity,
                unit,
                expiryDate.isEmpty() ? null : expiryDate
        );

        if (result != -1) {
            Toast.makeText(
                    this,
                    "Ingredient saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        } else {
            Toast.makeText(
                    this,
                    "Could not save ingredient. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

//    private void saveIngredient() {
//        String name = editName.getText().toString().trim();
//        String quantityText =
//                editQuantity.getText().toString().trim();
//        String unit = editUnit.getText().toString().trim();
//        String expiryDate =
//                editExpiryDate.getText().toString().trim();
//
//        // Check all required fields.
//        if (name.isEmpty()
//                || quantityText.isEmpty()
//                || unit.isEmpty()) {
//            Toast.makeText(
//                    this,
//                    "Please complete all required fields",
//                    Toast.LENGTH_SHORT
//            ).show();
//            return;
//        }
//
//        // Check that the quantity is a valid number.
//        double quantity;
//
//        try {
//            quantity = Double.parseDouble(quantityText);
//        } catch (NumberFormatException e) {
//            editQuantity.setError("Enter a valid quantity");
//            editQuantity.requestFocus();
//            return;
//        }
//
//        // Reject zero, negative values and non-finite numbers.
//        if (!Double.isFinite(quantity) || quantity <= 0) {
//            editQuantity.setError(
//                    "Quantity must be greater than zero"
//            );
//            editQuantity.requestFocus();
//            return;
//        }
//
//        // Database insertion will be added in Step 5.
//        Toast.makeText(
//                this,
//                "Form validation successful",
//                Toast.LENGTH_SHORT
//        ).show();
}
