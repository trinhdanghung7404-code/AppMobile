package com.example.thuoc.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MemberAdapter;
import com.example.thuoc.model.UserMedicine;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter memberAdapter;
    private final List<UserMedicine> memberList = new ArrayList<>();
    private final List<String> docIds = new ArrayList<>(); // lưu documentId tách riêng
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(memberAdapter);

        // 👉 Xử lý click item
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

        db = FirebaseFirestore.getInstance();

        // 👉 Lắng nghe thay đổi danh sách user
        db.collection("UserMedicine")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    memberList.clear();
                    docIds.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            UserMedicine u = doc.toObject(UserMedicine.class);
                            if (u != null) {
                                memberList.add(u);
                                docIds.add(doc.getId());
                            }
                        }
                        memberAdapter.notifyDataSetChanged();
                    }
                });

        // 👉 BottomNavigationView
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
                        String docId = String.valueOf(System.currentTimeMillis());

                        UserMedicine user = new UserMedicine(name, phone);

                        db.collection("UserMedicine")
                                .document(docId)
                                .set(user)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(this, "Đã thêm thành viên mới", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi tạo UserMedicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
