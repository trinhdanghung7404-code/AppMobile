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
import com.example.thuoc.model.MedicineEntry;
import com.example.thuoc.service.AlarmScheduler;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView rvMedicines;
    private MedicineEntryAdapter adapter;
    private List<MedicineEntry> medicineList;

    private FirebaseFirestore db;
    private String userId, userName;

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tvWelcome = findViewById(R.id.tvWelcomeUser);
        rvMedicines = findViewById(R.id.rvUserMedicines);

        medicineList = new ArrayList<>();
        adapter = new MedicineEntryAdapter(medicineList);

        // Không cho chỉnh sửa: chỉ xem, nên bỏ hết listener click/update
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        rvMedicines.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 🔹 Nhận userId & userName từ Intent khi login
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        tvWelcome.setText("Xin chào, " + (userName != null ? userName : "Người dùng"));

        loadUserMedicines();
    }

    /** Load danh sách thuốc của user */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void loadUserMedicines() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    medicineList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MedicineEntry med = doc.toObject(MedicineEntry.class);
                        if (med != null) {
                            med.setDocId(doc.getId());
                            medicineList.add(med);

                            // In ra để test times
                            if (med.getTimes() != null) {
                                Toast.makeText(this,
                                        "Thuốc " + med.getName() + " times: " + med.getTimes(),
                                        Toast.LENGTH_LONG).show();
                                System.out.println("DEBUG: " + med.getName() + " times = " + med.getTimes());
                            } else {
                                Toast.makeText(this,
                                        "Thuốc " + med.getName() + " chưa có times",
                                        Toast.LENGTH_SHORT).show();
                            }

                            AlarmScheduler.scheduleAlarmsForMedicine(this, med);
                        }
                    }
                    adapter.updateData(medicineList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
