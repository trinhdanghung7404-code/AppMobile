package com.example.thuoc.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.R;
import com.example.thuoc.controller.AuthController;
import com.example.thuoc.util.Constants;

public class RegisterActivity extends AppCompatActivity {

    private AuthController authController;
    private String role; // lưu role từ Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authController = new AuthController();

        // Lấy role từ Intent (được truyền từ MainActivity -> LoginActivity -> RegisterActivity)
        role = getIntent().getStringExtra(Constants.EXTRA_ROLE);
        if (role == null) role = Constants.ROLE_USER; // mặc định nếu không có

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            // Truyền role sang AuthController
            authController.registerUser(this, fullName, phone, pass, confirm, role);
        });

        tvGoLogin.setOnClickListener(v -> {
            finish(); // quay lại màn login
        });
    }
}
