package com.example.kellycode;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.ScanMode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_THEME_COLOR = "theme_color";

    private CodeScanner mCodeScanner;
    private TextView txt, textName;
    private CodeScannerView scannerView;
    final ObjectMapper mapper = new ObjectMapper();
    final OkHttpClient client = new OkHttpClient();
    Button btnScannerAgain, btnManualAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String colorPref = prefs.getString(KEY_THEME_COLOR, "yellow");
        if ("blue".equals(colorPref)) {
            setTheme(R.style.Theme_KellyCode_Blue);
        } else {
            setTheme(R.style.Theme_KellyCode_Yellow);
        }

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera);

        textName = findViewById(R.id.text_name);
        txt = findViewById(R.id.output);
        scannerView = findViewById(R.id.scanner_view);
        btnScannerAgain = findViewById(R.id.button_again);
        btnManualAgain = findViewById(R.id.button_again_manual);

        // Dynamické nastavení barev tlačítek a textu
        if ("blue".equals(colorPref)) {
            int white = ContextCompat.getColor(this, android.R.color.white);
            int primary = ContextCompat.getColor(this, R.color.blue);
            btnScannerAgain.setTextColor(white);
            btnManualAgain.setTextColor(white);
            findViewById(R.id.toolbar_top2).setBackgroundColor(primary);
            btnScannerAgain.setBackgroundColor(primary);
            btnManualAgain.setBackgroundColor(primary);
        } else {
            int black = ContextCompat.getColor(this, android.R.color.black);
            int primary = ContextCompat.getColor(this, R.color.yellow);
            btnScannerAgain.setTextColor(black);
            btnManualAgain.setTextColor(black);
            findViewById(R.id.toolbar_top2).setBackgroundColor(primary);
            btnScannerAgain.setBackgroundColor(primary);
            btnManualAgain.setBackgroundColor(primary);
        }

        textName.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        txt.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        setupPermissions();

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);
        mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setFlashEnabled(false);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            scannerView.setVisibility(View.GONE);
            btnScannerAgain.setVisibility(View.VISIBLE);
            textName.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
            sMethod(result.getText());
        }));

        mCodeScanner.setErrorCallback(thrown -> runOnUiThread(() -> Log.e("Main", "Camera initialization error:")));

        btnScannerAgain.setOnClickListener(v -> recreate());

        btnManualAgain.setOnClickListener(v -> {
            startActivity(new Intent(CameraActivity.this, MainActivity.class));
            finish();
        });

        String manualCode = getIntent().getStringExtra("manual_code");
        if (manualCode != null && !manualCode.isEmpty()) {
            scannerView.setVisibility(View.GONE);
            btnManualAgain.setVisibility(View.VISIBLE);
            textName.setVisibility(View.VISIBLE);
            txt.setVisibility(View.VISIBLE);
            sMethod(manualCode);
        } else {
            scannerView.setVisibility(View.VISIBLE);
            btnScannerAgain.setVisibility(View.GONE);
            btnManualAgain.setVisibility(View.GONE);
        }
    }

    public void sMethod(String out) {
        String json = "{\"searchText\":\"" + out + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://www.autokelly.cz/Search2/Product/Data")
                .post(body)
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("CameraActivity", "HTTP request failed", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String output = Objects.requireNonNull(response.body()).string();
                    String output2 = output.replace("\"products\":[{", "");
                    String json2 = output2.replace("]", "");
                    Map map = mapper.readValue(json2, Map.class);

                    try {
                        String code = Objects.requireNonNull(map.get("Code")).toString();
                        String name = Objects.requireNonNull(map.get("Name")).toString();

                        runOnUiThread(() -> {
                            textName.setText(name);
                            txt.setText(code);
                            textName.setVisibility(View.VISIBLE);
                            txt.setVisibility(View.VISIBLE);
                        });

                    } catch (NullPointerException e) {
                        Log.e("CameraActivity", "Response parsing failed", e);
                        runOnUiThread(() -> txt.setText(getString(R.string.try_again)));
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void setupPermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest();
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
