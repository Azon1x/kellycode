package com.example.kellycode;
import android.Manifest;
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
    private CodeScanner mCodeScanner;
    private TextView txt;
    final ObjectMapper mapper = new ObjectMapper();
    final OkHttpClient client = new OkHttpClient();
    Button btn;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera);
        txt = findViewById(R.id.output);
        btn = findViewById(R.id.button_again);
        setupPermissions();

        CodeScannerView scannerView = findViewById(R.id.scanner_view);

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setCamera(CodeScanner.CAMERA_BACK);
        mCodeScanner.setFormats(CodeScanner.ALL_FORMATS);
        mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
        mCodeScanner.setScanMode(ScanMode.SINGLE);
        mCodeScanner.setAutoFocusEnabled(true);
        mCodeScanner.setFlashEnabled(false);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> sMethod(result.getText())));

        mCodeScanner.setErrorCallback(thrown -> runOnUiThread(() -> Log.e("Main", "Camera initialization error:")));

        btn.setOnClickListener(v -> Restart());
    }
    public void Restart()
    {
        this.recreate();
    }

    public void sMethod(String out) {

        String json = "{\"searchText\":\"" + out + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://www.autokelly.cz/Search2/Product/Data")
                .post(body)
                .addHeader("cookie", "LKQEshop=CatalogSwitchViewType%3DRow%26Login%3DFalse; ASP.NET_SessionId=qw0qogymc2pxia3w4wmwf2fb")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Content-Type", "application/json;charset=utf-8")
                .addHeader("Origin", "https://www.autokelly.cz")
                .addHeader("Connection", "keep-alive")
                .addHeader("Referer", "https://www.autokelly.cz/Search/ResultList")
                .addHeader("Cookie", "LKQEshop=CatalogSwitchViewType=Row&Login=True; PersistentLogon_www_autokelly_cz=c229a510-7e91-4162-baf5-6735d9ce6d34; ASP.NET_SessionId=44uymlu2nl040spseqp5fcbk")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-origin")
                .addHeader("Pragma", "no-cache")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("TE", "trailers")
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
                        String damn = Objects.requireNonNull(map.get("Code")).toString();
                        CameraActivity.this.runOnUiThread(() -> txt.setText(damn));

                    }catch(NullPointerException e) {
                        Log.e("CameraActivity", "Response parsing failed", e);
                        CameraActivity.this.runOnUiThread(() -> txt.setText(getString(R.string.try_again)));

                    }
                }
            }
        });
        btn.setVisibility(View.VISIBLE);
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
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if(permission != PackageManager.PERMISSION_GRANTED){
            makeRequest();
        }
    }
    private void makeRequest(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
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