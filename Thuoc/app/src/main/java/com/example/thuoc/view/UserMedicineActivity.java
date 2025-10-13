package com.example.thuoc.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.TimePickerDialog;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MedicineEntryAdapter;
import com.example.thuoc.adapter.SelectMedicineAdapter;
import com.example.thuoc.model.Medicine;
import com.example.thuoc.model.MedicineEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class UserMedicineActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineEntryAdapter adapter;
    private FirebaseFirestore db;
    private String userId;
    private String userName; // tên user truyền sang
    private TextView tvTitle; // để update lại tên user sau khi sửa

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_medicine);

        // Nhận dữ liệu từ Intent
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        tvTitle = findViewById(R.id.tvTitleUserMedicine);
        if (userName != null) {
            tvTitle.setText("Danh sách thuốc của " + userName);
        }

        recyclerView = findViewById(R.id.recyclerViewUserMedicine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineEntryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Load thuốc
        loadUserMedicines(userId);

        // Click item -> chi tiết thuốc
        adapter.setOnItemClickListener((entry, pos) -> showMedicineDetailDialog(entry));

        // Nút back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút thêm thuốc
        findViewById(R.id.btnAddMedicine).setOnClickListener(v -> showSelectMedicineDialog());

        // Nút sửa user
        findViewById(R.id.btnEditUser).setOnClickListener(v -> showEditUserDialog());
    }

    private void loadUserMedicines(String userId) {
        if (userId == null || userId.isEmpty()) return;

        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<MedicineEntry> newList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        MedicineEntry entry = doc.toObject(MedicineEntry.class);
                        if (entry != null) {
                            entry.setDocId(doc.getId());
                            newList.add(entry);
                        }
                    }
                    adapter.updateData(newList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Hiện dialog chọn thuốc từ collection Medicine */
    private void showSelectMedicineDialog() {
        db.collection("Medicine")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Medicine> medicineList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Medicine m = doc.toObject(Medicine.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            medicineList.add(m);
                        }
                    }

                    SelectMedicineAdapter selectAdapter = new SelectMedicineAdapter(medicineList, selected -> {
                        addMedicineToUser(selected);
                    });

                    RecyclerView rv = new RecyclerView(this);
                    rv.setLayoutManager(new LinearLayoutManager(this));
                    rv.setAdapter(selectAdapter);

                    new AlertDialog.Builder(this)
                            .setTitle("Chọn thuốc để thêm")
                            .setView(rv)
                            .setNegativeButton("Đóng", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Thêm thuốc vào subcollection Medicines (tránh trùng lặp) */
    private void addMedicineToUser(Medicine medicine) {
        if (medicine == null || userId == null || userId.isEmpty()) return;

        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .whereEqualTo("name", medicine.getName()) // kiểm tra theo tên thuốc
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Đã tồn tại
                        Toast.makeText(this, "Thuốc " + medicine.getName() + " đã được thêm trước đó", Toast.LENGTH_SHORT).show();
                    } else {
                        // Chưa có → thêm mới
                        MedicineEntry entry = new MedicineEntry(
                                medicine.getName(),
                                "2 viên/ngày",
                                "Sáng - Tối"
                        );

                        db.collection("UserMedicine")
                                .document(userId)
                                .collection("Medicines")
                                .add(entry)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this, "Đã thêm thuốc cho " + userName, Toast.LENGTH_SHORT).show();
                                    loadUserMedicines(userId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showMedicineDetailDialog(MedicineEntry entry) {
        if (entry == null) return;

        // Inflate layout dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medicine_detail, null);

        TextView tvName = dialogView.findViewById(R.id.tvMedName);
        TextView tvDosage = dialogView.findViewById(R.id.tvMedDosage);
        TextView tvExpiry = dialogView.findViewById(R.id.tvMedExpiry);
        TextView tvTimes = dialogView.findViewById(R.id.tvMedTimes);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);

        // Gán dữ liệu
        tvName.setText(entry.getName());
        tvDosage.setText("Liều lượng: " + entry.getDosage());
        tvExpiry.setText("Ngày hết hạn: " +
                (entry.getExpiryDate() != null ? entry.getExpiryDate() : "Chưa có"));

        StringBuilder times = new StringBuilder();
        if (entry.getTimes() != null && !entry.getTimes().isEmpty()) {
            for (String t : entry.getTimes()) {
                times.append("\n- ").append(t);
            }
        } else {
            times.append("\nChưa có");
        }
        tvTimes.setText("Giờ uống:" + times);

        // Xử lý thêm giờ
        btnAddTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        String hh = String.format("%02d:%02d", hourOfDay, minute);
                        addTimeToMedicine(entry.getDocId(), hh);
                    }, 8, 0, true);
            tpd.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết thuốc")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .show();
    }

    /** Hàm thêm giờ uống vào Firestore */
    private void addTimeToMedicine(String entryDocId, String newTime) {
        if (userId == null || userId.isEmpty() || entryDocId == null || entryDocId.isEmpty()) return;

        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .document(entryDocId)
                .update("times", FieldValue.arrayUnion(newTime))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm giờ uống " + newTime, Toast.LENGTH_SHORT).show();
                    loadUserMedicines(userId); // reload danh sách
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi thêm giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Hiện dialog chỉnh sửa thông tin user */
    private void showEditUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        EditText etUserPhone = dialogView.findViewById(R.id.etUserPhone);
        Switch switchText = dialogView.findViewById(R.id.switchTextNotify);
        Switch switchVoice = dialogView.findViewById(R.id.switchVoiceNotify);

        // Load dữ liệu hiện tại từ Firestore
        db.collection("UserMedicine").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        Boolean textNotify = doc.getBoolean("textNotify");
                        Boolean voiceNotify = doc.getBoolean("voiceNotify");

                        etUserName.setText(name != null ? name : "");
                        etUserPhone.setText(phone != null ? phone : "");
                        switchText.setChecked(textNotify != null && textNotify);
                        switchVoice.setChecked(voiceNotify != null && voiceNotify);
                    }
                });

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = etUserName.getText().toString().trim();
                    String newPhone = etUserPhone.getText().toString().trim();
                    boolean textNotify = switchText.isChecked();
                    boolean voiceNotify = switchVoice.isChecked();

                    db.collection("UserMedicine")
                            .document(userId)
                            .update(
                                    "name", newName,
                                    "phone", newPhone,
                                    "textNotify", textNotify,
                                    "voiceNotify", voiceNotify
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                                tvTitle.setText("Danh sách thuốc của " + newName);
                                userName = newName; // update lại userName
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
