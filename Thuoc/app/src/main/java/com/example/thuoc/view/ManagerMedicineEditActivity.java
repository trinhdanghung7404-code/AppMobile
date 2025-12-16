package com.example.thuoc.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MedicineAdapter;
import com.example.thuoc.dao.MedicineDAO;
import com.example.thuoc.model.Medicine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ManagerMedicineEditActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMedicine;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList;
    private MedicineDAO medicineDAO;
    private FirebaseFirestore db;
    private FloatingActionButton fabAdd, fabDelete;

    // 🚩 THAY ĐỔI 1: Thêm biến để lưu trữ User ID
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        // 🚩 THAY ĐỔI 2: Lấy User ID từ Intent
        currentUserId = getIntent().getStringExtra("userId");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy User ID", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity nếu không có ID
            return;
        }

        recyclerViewMedicine = findViewById(R.id.recyclerViewMedicine);
        recyclerViewMedicine.setLayoutManager(new LinearLayoutManager(this));

        medicineList = new ArrayList<>();
        adapter = new MedicineAdapter(medicineList);
        recyclerViewMedicine.setAdapter(adapter);

        medicineDAO = new MedicineDAO();
        db = FirebaseFirestore.getInstance();

        loadMedicines(); // Gọi hàm load thuốc với userId

        // 🔹 Liên kết các nút FloatingActionButton
        fabAdd = findViewById(R.id.fabAddMedicine);
        fabDelete = findViewById(R.id.fabDeleteMedicine);

        fabAdd.setOnClickListener(v -> showAddMedicineDialog());
        fabDelete.setOnClickListener(v -> deleteSelectedMedicines());

        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navTask = findViewById(R.id.nav_task);
        ImageView btnAccount = findViewById(R.id.btnAccount);

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, ManagerDashboardActivity.class);
            i.putExtra("userId", currentUserId); // Sử dụng biến đã lưu
            startActivity(i);
            overridePendingTransition(0, 0);
        });

        navTask.setOnClickListener(v -> {
            // Đang ở màn này nên không cần chuyển
        });

        btnAccount.setOnClickListener(v -> {
            Intent i = new Intent(this, ManagerAccountActivity.class);
            i.putExtra("userId", currentUserId); // Sử dụng biến đã lưu
            startActivity(i);
        });

    }

    // 🔹 Load danh sách thuốc
    private void loadMedicines() {
        // 🚩 THAY ĐỔI 3: Truyền currentUserId vào MedicineDAO để lọc thuốc
        medicineDAO.getAllMedicines(
                currentUserId, // <-- Truyền userId vào đây
                newList -> adapter.updateData(newList),
                error -> Toast.makeText(this, "Lỗi load dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // 🗑️ Xoá thuốc được chọn
    private void deleteSelectedMedicines() {
        Set<String> selectedIds = adapter.getSelectedIds();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Chưa chọn thuốc để xoá", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa thuốc")
                .setMessage("Bạn có chắc muốn xoá " + selectedIds.size() + " thuốc đã chọn?")
                .setPositiveButton("Xoá", (dialog, which) ->
                        medicineDAO.deleteMedicines(
                                selectedIds,
                                () -> {
                                    Toast.makeText(this, "Đã xoá thuốc", Toast.LENGTH_SHORT).show();
                                    selectedIds.clear();
                                    loadMedicines();
                                },
                                e -> Toast.makeText(
                                        this,
                                        "Lỗi khi xoá: " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show()
                        )
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ➕ Thêm thuốc (Đã sửa để thêm userId)
    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);

        EditText etName = dialogView.findViewById(R.id.etMedicineName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);

        etExpiry.setFocusable(false);
        etExpiry.setClickable(true);
        etExpiry.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, y, m, d) -> etExpiry.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                    ),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Thêm thuốc")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {

                    String name = etName.getText().toString().trim();
                    String qtyStr = etQuantity.getText().toString().trim();
                    String expiry = etExpiry.getText().toString().trim();

                    if (name.isEmpty() || qtyStr.isEmpty() || expiry.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int qty;
                    try {
                        qty = Integer.parseInt(qtyStr);
                        if (qty < 0) throw new NumberFormatException();
                    } catch (Exception e) {
                        Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    medicineDAO.addMedicine(
                            name,
                            expiry,
                            qty,
                            "viên",
                            currentUserId,
                            () -> {
                                Toast.makeText(this, "Đã thêm thuốc", Toast.LENGTH_SHORT).show();
                                loadMedicines();
                            },
                            e -> Toast.makeText(this,
                                    "Lỗi khi thêm thuốc: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}