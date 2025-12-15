package com.example.thuoc.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        etPhone = findViewById(R.id.etUserPhone);
        btnLogin = findViewById(R.id.btnUserLogin);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            checkUserLogin(phone);
        });
    }

    private void checkUserLogin(String phone) {
        db.collection("UserMedicine")
                .whereEqualTo("phone", phone) // 🔹 kiểm tra theo field "phone"
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String usermedId = doc.getId();
                        String userName = doc.getString("userName");
                        String userId    = doc.getString("userId");

                        // Chuyển sang UserMedicineActivity
                        Intent i = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                        i.putExtra("usermedId", usermedId);
                        i.putExtra("userName", userName);
                        i.putExtra("userId", userId);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Không tìm thấy số điện thoại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
