package com.example.smartpantrymanagement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Find the views from the XML layout.
        TextView settingsTitle = findViewById(R.id.settingsTitle);
        Switch expirySwitch = findViewById(R.id.switchExpiryAlerts);

        // Open the preferences file.
        SharedPreferences preferences =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Read the saved setting.
        // Expiry alerts are enabled by default.
        boolean alertsEnabled =
                preferences.getBoolean(KEY_EXPIRY_ALERTS, true);

        // Display the saved setting on the switch.
        expirySwitch.setChecked(alertsEnabled);

        // Save every change made to the switch.
        expirySwitch.setOnCheckedChangeListener((button, checked) -> {
            preferences.edit()
                    .putBoolean(KEY_EXPIRY_ALERTS, checked)
                    .apply();
        });
    }
}
