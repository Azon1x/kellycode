package com.example.kellycode;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public final class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_THEME_COLOR = "theme_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Nastavení tématu před zobrazením UI
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String colorPref = prefs.getString(KEY_THEME_COLOR, "yellow");
        if ("blue".equals(colorPref)) {
            setTheme(R.style.Theme_KellyCode_Blue);
        } else {
            setTheme(R.style.Theme_KellyCode_Yellow);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logoAutoKelly = findViewById(R.id.logo);
        ImageView logoLkq = findViewById(R.id.logo2);

        if ("blue".equals(colorPref)) {
            logoAutoKelly.setVisibility(View.INVISIBLE);
            logoLkq.setVisibility(View.VISIBLE);
        } else {
            logoAutoKelly.setVisibility(View.VISIBLE);
            logoLkq.setVisibility(View.INVISIBLE);
        }

        // Skenovací tlačítko
        Button btn = findViewById(R.id.button_scan);
        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));

        // Ruční vstup
        TextInputEditText manualInput = findViewById(R.id.manual_input);
        manualInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {

                String code = manualInput.getText() != null ? manualInput.getText().toString().trim() : "";
                if (!code.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    intent.putExtra("manual_code", code);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        // ImageButton pro změnu barevného tématu
        ImageButton themeToggleButton = findViewById(R.id.account);
        themeToggleButton.setOnClickListener(v -> {
            String newTheme = "yellow".equals(colorPref) ? "blue" : "yellow";
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_THEME_COLOR, newTheme);
            editor.apply();

            // Restart aktivity
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
