package com.example.thuoc.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.MainActivity;
import com.example.thuoc.R;
import com.example.thuoc.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserAccountActivity extends AppCompatActivity {
    private TextView tvName, tvPhone;
    private Button btnLogout;
    private FirebaseFirestore db;

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        btnLogout = findViewById(R.id.btnLogout);

        db = FirebaseFirestore.getInstance();

        // 🔹 Lấy userId từ SharedPreferences (lưu khi đăng nhập)
        String userId = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                .getString("user_id", null);

        if (userId != null) {
            loadUserInfo(userId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                    .edit().clear().apply();

            Intent intent = new Intent(UserAccountActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserInfo(String userId) {
        DocumentReference docRef = db.collection("Users").document(userId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvName.setText("Họ tên: " + user.getName());
                            tvPhone.setText("Số điện thoại: " + user.getPhone());
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }
}
