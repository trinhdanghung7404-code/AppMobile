package com.example.thuoc.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.R;
import com.example.thuoc.controller.AuthController;

public class ManagerLoginActivity extends AppCompatActivity {

    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authController = new AuthController();

        EditText etPhone = findViewById(R.id.etPhone);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(ManagerLoginActivity.this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Truyền đúng Activity context, không phải getApplicationContext()
            Log.d("LoginActivity", "Đăng nhập với: " + phone);
            authController.loginUser(ManagerLoginActivity.this, phone, pass);
        });

        tvGoRegister.setOnClickListener(v -> {
            Intent i = new Intent(ManagerLoginActivity.this, ManagerRegisterActivity.class);
            startActivity(i);
        });
    }
}
