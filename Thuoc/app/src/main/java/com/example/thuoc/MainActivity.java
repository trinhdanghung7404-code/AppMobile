package com.example.thuoc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.util.Constants;
import com.example.thuoc.view.LoginActivity;
import com.example.thuoc.view.UserLoginActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnManager = findViewById(R.id.btnManager);
        Button btnUser = findViewById(R.id.btnUser);

        btnManager.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        });

        btnUser.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(i);
        });
    }
}
