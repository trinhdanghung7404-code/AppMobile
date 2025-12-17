package com.example.thuoc.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.R;
import com.example.thuoc.controller.AuthController;
import com.example.thuoc.util.Constants;

public class ManagerRegisterActivity extends AppCompatActivity {

    private AuthController authController;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authController = new AuthController();

        // Nhận role từ Intent
        role = getIntent().getStringExtra(Constants.EXTRA_ROLE);
        if (role == null) role = Constants.ROLE_USER;

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        // Đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullName.setError("Vui lòng nhập họ tên");
                return;
            }

            // Vì manager không nhập tên => tên để trong AuthController
            authController.registerManager(
                    this,
                    fullName,
                    phone,
                    email,
                    pass,
                    confirm
            );

        });

        // Quay lại login
        tvGoLogin.setOnClickListener(v -> finish());
    }
}
