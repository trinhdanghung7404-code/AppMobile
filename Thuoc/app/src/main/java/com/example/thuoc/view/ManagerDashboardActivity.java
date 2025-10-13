package com.example.thuoc.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MemberAdapter;
import com.example.thuoc.dao.UserMedicineDAO;
import com.example.thuoc.model.UserMedicine;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter memberAdapter;
    private final List<UserMedicine> memberList = new ArrayList<>();
    private final List<String> docIds = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private UserMedicineDAO userMedicineDAO;
    private ListenerRegistration listenerRegistration;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        // --- Khởi tạo giao diện ---
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(memberAdapter);

        // --- Xử lý click item ---
        memberAdapter.setOnItemClickListener((userMed, position) -> {
            String docId = docIds.get(position);
            Intent i = new Intent(ManagerDashboardActivity.this, UserMedicineActivity.class);
            i.putExtra("userId", docId);
            i.putExtra("userName", userMed.getUserName());
            i.putExtra("userPhone", userMed.getPhone());
            startActivity(i);
        });

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddMemberDialog());

        // --- Khởi tạo DAO ---
        userMedicineDAO = new UserMedicineDAO();

        // --- Lấy userId từ Intent ---
        String currentUserId = getIntent().getStringExtra("userId");

        // --- Lắng nghe thay đổi dữ liệu qua DAO ---
        listenerRegistration = userMedicineDAO.listenAll(currentUserId, new UserMedicineDAO.UserMedicineListener() {
            @Override
            public void onDataChange(List<UserMedicine> users, List<String> ids) {
                memberList.clear();
                memberList.addAll(users);
                docIds.clear();
                docIds.addAll(ids);
                memberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ManagerDashboardActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnAccount = findViewById(R.id.btnAccount);
        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserAccountActivity.class);
            startActivity(intent);
        });

        // --- Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(this, MedicineActivity.class));
                return true;
            }
            return false;
        });
    }

    // --- Thêm thành viên mới ---
    private void showAddMemberDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);

        new AlertDialog.Builder(this)
                .setTitle("Thêm thành viên")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();

                    if (!name.isEmpty() && !phone.isEmpty()) {
                        // 🔹 Lấy ID người dùng hiện tại
                        String currentUserId = getIntent().getStringExtra("userId");
                        Log.d("AddMember", "✅ currentUserId from Intent: " + currentUserId);
                        // 🔹 Tạo đối tượng và gán userId
                        UserMedicine newUser = new UserMedicine(name, phone);
                        newUser.setUserId(currentUserId);

                        // 🔹 Thêm vào Firestore qua DAO
                        userMedicineDAO.addUserMedicine(currentUserId, newUser, new UserMedicineDAO.AddUserCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ManagerDashboardActivity.this,
                                        "Đã thêm thành viên mới", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ManagerDashboardActivity.this,
                                        "Lỗi tạo thành viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
