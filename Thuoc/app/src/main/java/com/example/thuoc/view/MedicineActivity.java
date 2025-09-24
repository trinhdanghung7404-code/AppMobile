package com.example.thuoc.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

public class MedicineActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMedicine;
    private MedicineAdapter adapter;
    private List<Medicine> medicineList;
    private MedicineDAO medicineDAO; // dùng DAO

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

        loadMedicines();

        FloatingActionButton fabAdd = findViewById(R.id.fabAddMedicine);
        fabAdd.setOnClickListener(v -> showAddMedicineDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent i = new Intent(MedicineActivity.this, ManagerDashboardActivity.class);
                startActivity(i);
                finish();
                return true;
            } else if (id == R.id.nav_list) {
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_list);
    }

    private void loadMedicines() {
        medicineDAO.getAllMedicines(
                newList -> adapter.updateData(newList),
                error -> Toast.makeText(this, "Lỗi load dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showAddMedicineDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medicine, null);

        EditText etName = dialogView.findViewById(R.id.etMedicineName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        EditText etExpiry = dialogView.findViewById(R.id.etExpiry);

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

                    Medicine med = new Medicine(null, name, expiry, qty, "viên");

                    medicineDAO.addMedicine(
                            med,
                            () -> Toast.makeText(this, "Đã thêm thuốc: " + name, Toast.LENGTH_SHORT).show(),
                            e -> Toast.makeText(this, "Lỗi khi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
