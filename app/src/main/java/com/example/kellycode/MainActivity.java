package com.example.kellycode;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public final class MainActivity extends AppCompatActivity {
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.button_scan);
        btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
    }
}
