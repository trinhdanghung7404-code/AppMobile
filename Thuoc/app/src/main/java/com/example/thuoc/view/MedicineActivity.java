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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MedicineActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMedicine;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList;
    private MedicineDAO medicineDAO;
    private FirebaseFirestore db;
    private FloatingActionButton fabAdd, fabDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        recyclerViewMedicine = findViewById(R.id.recyclerViewMedicine);
        recyclerViewMedicine.setLayoutManager(new LinearLayoutManager(this));

        medicineList = new ArrayList<>();
        adapter = new MedicineAdapter(medicineList);
        recyclerViewMedicine.setAdapter(adapter);

        medicineDAO = new MedicineDAO();
        db = FirebaseFirestore.getInstance();

        loadMedicines();

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
            i.putExtra("userId", getIntent().getStringExtra("userId"));
            startActivity(i);
            overridePendingTransition(0, 0);
        });

        navTask.setOnClickListener(v -> {
            // Đang ở màn này nên không cần chuyển
        });

        btnAccount.setOnClickListener(v -> {
            Intent i = new Intent(this, UserAccountActivity.class);
            i.putExtra("userId", getIntent().getStringExtra("userId"));
            startActivity(i);
        });

    }

    // 🔹 Load danh sách thuốc
    private void loadMedicines() {
        medicineDAO.getAllMedicines(
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
                .setPositiveButton("Xoá", (dialog, which) -> {
                    for (String id : selectedIds) {
                        db.collection("Medicine").document(id)
                                .delete()
                                .addOnSuccessListener(v ->
                                        Toast.makeText(this, "Đã xoá thuốc ID " + id, Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lỗi khi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    selectedIds.clear();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ➕ Thêm thuốc (giữ nguyên logic của bạn)
    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);

        EditText etName = dialogView.findViewById(R.id.etMedicineName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);

        // 🔹 Thêm DatePicker cho ô hạn sử dụng
        etExpiry.setFocusable(false);
        etExpiry.setClickable(true);
        etExpiry.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        etExpiry.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
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
                        if (qty < 0) {
                            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Số lượng phải là số", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 🔹 Sinh ID tăng dần dựa trên số thuốc hiện có
                    db.collection("Medicine")
                            .get()
                            .addOnSuccessListener(query -> {
                                int nextId = query.size() + 1;
                                String id = String.valueOf(nextId);

                                Medicine med = new Medicine(id, name, expiry, qty, "viên");
                                medicineDAO.addMedicine(
                                        med,
                                        () -> Toast.makeText(this, "Đã thêm thuốc #" + id, Toast.LENGTH_SHORT).show(),
                                        e -> Toast.makeText(this, "Lỗi khi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Không thể tạo ID: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
