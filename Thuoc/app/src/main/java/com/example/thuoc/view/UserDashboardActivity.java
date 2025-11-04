package com.example.thuoc.view;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MedicineEntryAdapter;
import com.example.thuoc.dao.UserMedicineDAO;
import com.example.thuoc.model.MedicineEntry;
import com.example.thuoc.service.AlarmScheduler;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView rvMedicines;
    private MedicineEntryAdapter adapter;
    private List<MedicineEntry> medicineList;

    private String usermedId, userName;
    private UserMedicineDAO userMedicineDAO;

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tvWelcome = findViewById(R.id.tvWelcomeUser);
        rvMedicines = findViewById(R.id.rvUserMedicines);

        medicineList = new ArrayList<>();
        adapter = new MedicineEntryAdapter(medicineList);
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        rvMedicines.setAdapter(adapter);

        userMedicineDAO = new UserMedicineDAO();

        usermedId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        tvWelcome.setText("Xin chào, " + (userName != null ? userName : "Người dùng"));

        loadUserMedicines();
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void loadUserMedicines() {
        if (usermedId == null || usermedId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        userMedicineDAO.getMedicinesByUserId(usermedId, medicines -> {
            medicineList.clear();
            medicineList.addAll(medicines);
            adapter.updateData(medicineList);

            // Lên lịch thông báo cho từng thuốc
            for (MedicineEntry med : medicines) {
                AlarmScheduler.scheduleAlarmsForMedicine(this, med, usermedId);
            }

        }, e -> Toast.makeText(this, "Lỗi khi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
