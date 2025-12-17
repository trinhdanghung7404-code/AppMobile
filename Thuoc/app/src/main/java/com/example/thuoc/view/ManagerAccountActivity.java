package com.example.thuoc.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.MainActivity;
import com.example.thuoc.R;
import com.example.thuoc.adapter.ManagedUserAdapter;
import com.example.thuoc.dao.UserDAO;
import com.example.thuoc.model.User;
import com.example.thuoc.model.UserMedicine;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManagerAccountActivity extends AppCompatActivity {

    private TextView tvName, tvPhone;
    private Button btnLogout;
    private RecyclerView rvManagedUsers;

    private FirebaseFirestore db;
    private ManagedUserAdapter adapter;
    private UserDAO userDAO;

    // 👉 Adapter sẽ giữ cả user + usermedId
    private final List<UserMedicine> managedUsers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        // ===== View =====
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        btnLogout = findViewById(R.id.btnLogout);
        rvManagedUsers = findViewById(R.id.rvManagedUsers);
        ImageButton btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        // ===== RecyclerView =====
        rvManagedUsers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ManagedUserAdapter(managedUsers,
                (usermedId, user) -> {
                    Intent i = new Intent(this, ManagerMedicationHistoryActivity.class);
                    i.putExtra("usermedId", usermedId);   // ⭐ documentId
                    i.putExtra("userName", user.getUserName());
                    startActivity(i);
                });
        rvManagedUsers.setAdapter(adapter);

        userDAO = new UserDAO();
        String managerId = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                .getString("user_id", null);

        if (managerId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        loadUserInfo(managerId);
        loadManagedUsers(managerId);

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                    .edit().clear().apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // ================= LOAD USER INFO =================
    private void loadUserInfo(String managerId) {
        userDAO.loadManagerInfo(
                managerId,
                user -> {
                    if (user == null) return;
                    tvName.setText("Họ tên: " + user.getName());
                    tvPhone.setText("Số điện thoại: " + user.getPhone());
                },
                e -> Toast.makeText(this,
                        "Lỗi tải thông tin người dùng",
                        Toast.LENGTH_SHORT).show()
        );
    }

    // ================= LOAD MANAGED USERS =================
    private void loadManagedUsers(String managerId) {
        userDAO.loadManagedUsers(
                managerId,
                users -> {
                    managedUsers.clear();
                    managedUsers.addAll(users);
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(this,
                        "Lỗi tải danh sách quản lý",
                        Toast.LENGTH_SHORT).show()
        );
    }
}
