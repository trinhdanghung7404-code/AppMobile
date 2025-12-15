package com.example.thuoc.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MemberAdapter;
import com.example.thuoc.dao.UserMedicineDAO;
import com.example.thuoc.model.UserMedicine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MemberAdapter memberAdapter;
    private final List<UserMedicine> memberList = new ArrayList<>();
    private final List<String> docIds = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private UserMedicineDAO umDAO;
    private ListenerRegistration listenerRegistration;
    private String selectedAvatarType = "boy"; // mặc định

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        umDAO = new UserMedicineDAO();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(memberAdapter);

        memberAdapter.setOnItemClickListener((userMed, position) -> {
            String usermedId = docIds.get(position);
            String managerId = getIntent().getStringExtra("userId");

            Intent i = new Intent(ManagerDashboardActivity.this, ManagerUserMedicineEditActivity.class);
            i.putExtra("usermedId", usermedId);
            i.putExtra("managerId", managerId);
            i.putExtra("userName", userMed.getUserName());
            i.putExtra("userPhone", userMed.getPhone());
            startActivity(i);
        });

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> showAddMemberDialog());

        String currentUserId = getIntent().getStringExtra("userId");

        listenerRegistration = umDAO.listenAll(currentUserId, new UserMedicineDAO.UserMedicineListener() {
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

        ImageView btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(v -> showHelpDialog());
        
        // bottom nav
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navTask = findViewById(R.id.nav_task);
        ImageView btnAccount = findViewById(R.id.btnAccount);

        navTask.setOnClickListener(v -> {
            Intent i = new Intent(this, ManagerMedicineEditActivity.class);
            i.putExtra("userId", getIntent().getStringExtra("userId"));
            startActivity(i);
            overridePendingTransition(0, 0);
        });

        btnAccount.setOnClickListener(v -> {
            Intent i = new Intent(this, ManagerAccountActivity.class);
            i.putExtra("userId", getIntent().getStringExtra("userId"));
            startActivity(i);
        });
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn sử dụng")
                .setMessage("• Nhấn nút '+' để thêm thành viên mới.\n" +
                        "• Chạm vào tên thành viên để xem chi tiết thuốc của họ.\n" +
                        "• Dùng thanh điều hướng bên dưới để chuyển giữa các chức năng.\n" +
                        "• Nếu cần chỉnh sửa hoặc xóa, hãy nhấn giữ vào thành viên.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private void showAddMemberDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        EditText edtName = dialogView.findViewById(R.id.edtName);
        EditText edtPhone = dialogView.findViewById(R.id.edtPhone);

        // nhóm avatar (phải trùng với dialog_add_member.xml)
        int[] avatarIds = {
                R.id.avtBoy, R.id.avtGirl, R.id.avtMan,
                R.id.avtWoman, R.id.avtGrandpa, R.id.avtGrandma
        };
        String[] avatarTypes = {"boy", "girl", "man", "woman", "grandpa", "grandma"};

        ImageView[] avatarViews = new ImageView[avatarIds.length];
        for (int i = 0; i < avatarIds.length; i++) {
            avatarViews[i] = dialogView.findViewById(avatarIds[i]);
        }

        // Xử lý chọn avatar
        for (int i = 0; i < avatarViews.length; i++) {
            int index = i;
            avatarViews[i].setOnClickListener(v -> {
                selectedAvatarType = avatarTypes[index];

                // reset alpha để làm hiệu ứng chọn
                for (ImageView img : avatarViews) img.setAlpha(0.4f);
                v.setAlpha(1f);
            });
        }

        new AlertDialog.Builder(this)
                .setTitle("Thêm thành viên")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();

                    try {
                        if (name.isEmpty() || phone.isEmpty()) {
                            throw new IllegalArgumentException("Vui lòng nhập đủ thông tin");
                        }

                        if (!phone.matches("\\d+")) {
                            throw new NumberFormatException("Số điện thoại chỉ được chứa chữ số");
                        }

                        String currentUserId = getIntent().getStringExtra("userId");
                        UserMedicine newUser = new UserMedicine(name, phone);
                        newUser.setUserId(currentUserId);
                        newUser.setAvatarType(selectedAvatarType);

                        umDAO.addUserMedicine(currentUserId, newUser, new UserMedicineDAO.AddUserCallback() {
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

                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi không xác định: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
