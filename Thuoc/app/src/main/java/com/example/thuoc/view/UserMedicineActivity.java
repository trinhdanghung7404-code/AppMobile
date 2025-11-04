package com.example.thuoc.view;

import android.os.Bundle;
import android.text.InputType;
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
import java.util.Map;

public class UserMedicineActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineEntryAdapter adapter;
    private final MedicineEntryDAO meDAO = new MedicineEntryDAO();
    private final MedicineDAO mDAO = new MedicineDAO();
    private final UserMedicineDAO userMedicineDAO = new UserMedicineDAO();
    private FirebaseFirestore db;
    private String userId;
    private String userName;
    private TextView tvTitle;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_medicine);

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

        loadUserMedicines(userId);

        adapter.setOnItemClickListener((entry, pos) -> showMedicineDetailDialog(entry));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddMedicine).setOnClickListener(v -> showSelectMedicineDialog());

        findViewById(R.id.btnEditUser).setOnClickListener(v -> showEditUserDialog());
    }

    private void loadUserMedicines(String userId) {
        meDAO.getMedicinesByUserId(userId, list -> {
            adapter.updateData(list);
        }, e -> {
            Toast.makeText(this, "Lỗi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showSelectMedicineDialog() {
        mDAO.getMedicines(medicineList -> {
            SelectMedicineAdapter adapter = new SelectMedicineAdapter(medicineList, selected -> {
                addMedicineToUser(selected);
            });

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_medicine, null);
            RecyclerView rv = dialogView.findViewById(R.id.rvMedicines);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(adapter);

            new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setNegativeButton("Đóng", null)
                    .show();

        }, e -> {
            Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addMedicineToUser(Medicine medicine) {
        meDAO.addMedicine(userId, medicine,
                () -> {
                    Toast.makeText(this, "Đã thêm thuốc cho " + userName, Toast.LENGTH_SHORT).show();
                    loadUserMedicines(userId);
                },
                e -> Toast.makeText(this, "Lỗi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showMedicineDetailDialog(MedicineEntry entry) {
        if (entry == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medicine_detail, null);
        TextView tvName = dialogView.findViewById(R.id.tvMedName);
        TextView tvExpiry = dialogView.findViewById(R.id.tvMedExpiry);
        TextView tvTimes = dialogView.findViewById(R.id.tvMedTimes);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);

        tvName.setText(entry.getName());
        tvExpiry.setText("Ngày hết hạn: " +
                (entry.getExpiryDate() != null ? entry.getExpiryDate() : "Chưa có"));

        StringBuilder times = new StringBuilder();
        if (entry.getTimes() != null && !entry.getTimes().isEmpty()) {
            for (Map<String, String> t : entry.getTimes()) {
                String time = t.get("time");
                String dosage = t.get("dosage");
                times.append("\n- ").append(time);
                if (dosage != null && !dosage.isEmpty()) {
                    times.append(": ").append(dosage);
                }
            }
        } else {
            times.append("\nChưa có");
        }
        tvTimes.setText("Giờ & liều lượng:" + times.toString());

        btnAddTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        String hh = String.format("%02d:%02d", hourOfDay, minute);

                        EditText input = new EditText(this);
                        input.setHint("Nhập liều lượng, ví dụ: 1 viên");

                        new AlertDialog.Builder(this)
                                .setTitle("Nhập liều lượng cho " + hh)
                                .setView(input)
                                .setPositiveButton("Lưu", (d, w) -> {
                                    String dosage = input.getText().toString().trim();
                                    if (!dosage.isEmpty()) {
                                        meDAO.addTime(userId, entry.getDocId(), hh, dosage,
                                                () -> {
                                                    Toast.makeText(this, "Đã thêm: " + hh + " - " + dosage, Toast.LENGTH_SHORT).show();
                                                    loadUserMedicines(userId);
                                                },
                                                e -> Toast.makeText(this, "Lỗi khi thêm giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                                    } else {
                                        Toast.makeText(this, "Chưa nhập liều lượng", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }, 8, 0, true);
            tpd.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chi tiết thuốc")
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Xóa thuốc", null)
                .create();

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
                            }, e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        });

        dialog.show();
    }

    private void showEditUserDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        EditText etUserPhone = dialogView.findViewById(R.id.etUserPhone);
        Switch switchText = dialogView.findViewById(R.id.switchTextNotify);
        Switch switchVoice = dialogView.findViewById(R.id.switchVoiceNotify);

        UserMedicineDAO userMedicineDAO = new UserMedicineDAO();

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
                .setNeutralButton("Xóa người dùng", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa người dùng này không? Toàn bộ thuốc sẽ bị xóa.")
                    .setPositiveButton("Xóa", (d2, w2) -> {
                        userMedicineDAO.deleteUser(userId, () -> {
                            Toast.makeText(this, "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                            finish();
                        }, e -> {
                            Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }
}
