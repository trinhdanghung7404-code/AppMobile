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
import com.example.thuoc.dao.MedicineDAO;
import com.example.thuoc.dao.MedicineEntryDAO;
import com.example.thuoc.dao.UserMedicineDAO;
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
    private final MedicineEntryDAO medDao = new MedicineEntryDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final UserMedicineDAO userMedicineDAO = new UserMedicineDAO();
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
        medDao.getMedicinesByUserId(userId, list -> {
            adapter.updateData(list);
        }, e -> {
            Toast.makeText(this, "Lỗi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /** Hiện dialog chọn thuốc từ collection Medicine */
    private void showSelectMedicineDialog() {

        medicineDAO.getMedicines(medicineList -> {
            SelectMedicineAdapter adapter = new SelectMedicineAdapter(medicineList, selected -> {
                addMedicineToUser(selected);
            });

            RecyclerView rv = new RecyclerView(this);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(adapter);

            new AlertDialog.Builder(this)
                    .setTitle("Chọn thuốc để thêm")
                    .setView(rv)
                    .setNegativeButton("Đóng", null)
                    .show();

        }, e -> {
            Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    /** Thêm thuốc vào subcollection Medicines (tránh trùng lặp) */
    private void addMedicineToUser(Medicine medicine) {
        medDao.addMedicine(userId, medicine,
                () -> {
                    Toast.makeText(this, "Đã thêm thuốc cho " + userName, Toast.LENGTH_SHORT).show();
                    loadUserMedicines(userId);
                },
                e -> Toast.makeText(this, "Lỗi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
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

        // Gán dữ liệu thuốc
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

        // 🔹 Tạo dialog chi tiết thuốc
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chi tiết thuốc")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Xóa thuốc", null) // Đặt nút xóa thuốc
                .create();

        // 🔹 Sau khi show(), mới bắt sự kiện nút được
        dialog.setOnShowListener(dlg -> {
            Button btnDeleteMed = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            btnDeleteMed.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa thuốc \"" + entry.getName() + "\" không?")
                        .setPositiveButton("Xóa", (d2, w2) -> {
                            userMedicineDAO.deleteMedicine(userId, entry.getDocId(), () -> {
                                Toast.makeText(this, "Đã xóa thuốc", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }, e -> {
                                Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        });

        dialog.show();
    }


    /** Hàm thêm giờ uống vào Firestore */
    private void addTimeToMedicine(String entryDocId, String newTime) {
        medDao.addTime(userId, entryDocId, newTime,
                () -> {
                    Toast.makeText(this, "Đã thêm giờ uống " + newTime, Toast.LENGTH_SHORT).show();
                    loadUserMedicines(userId); // reload danh sách sau khi thêm
                },
                e -> Toast.makeText(this, "Lỗi khi thêm giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    /** Hiện dialog chỉnh sửa thông tin user */
    private void showEditUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        EditText etUserPhone = dialogView.findViewById(R.id.etUserPhone);
        Switch switchText = dialogView.findViewById(R.id.switchTextNotify);
        Switch switchVoice = dialogView.findViewById(R.id.switchVoiceNotify);

        UserMedicineDAO userMedicineDAO = new UserMedicineDAO();

        // 🔹 Load thông tin user hiện tại
        userMedicineDAO.getUserInfo(userId, user -> {
            etUserName.setText(user.getUserName() != null ? user.getUserName() : "");
            etUserPhone.setText(user.getPhone() != null ? user.getPhone() : "");
            switchText.setChecked(user.isTextNotify());
            switchVoice.setChecked(user.isVoiceNotify());
        }, e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin")
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = etUserName.getText().toString().trim();
                    String newPhone = etUserPhone.getText().toString().trim();
                    boolean textNotify = switchText.isChecked();
                    boolean voiceNotify = switchVoice.isChecked();

                    userMedicineDAO.updateUserInfo(userId, newName, newPhone, textNotify, voiceNotify, () -> {
                        Toast.makeText(this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                        tvTitle.setText("Danh sách thuốc của " + newName);
                        userName = newName;
                    }, e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa người dùng", null) // tạm để null để set custom handler
                .create();

        dialog.show();

        // 🔹 Bắt sự kiện "Xóa người dùng"
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa người dùng này không? Toàn bộ thuốc sẽ bị xóa.")
                    .setPositiveButton("Xóa", (d2, w2) -> {
                        userMedicineDAO.deleteUser(userId, () -> {
                            Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                            finish(); // đóng Activity sau khi xóa
                        }, e -> {
                            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
