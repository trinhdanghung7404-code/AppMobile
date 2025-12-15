package com.example.thuoc.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.thuoc.adapter.MemberAdapter;
import com.example.thuoc.adapter.SelectMedicineAdapter;
import com.example.thuoc.dao.MedicineDAO;
import com.example.thuoc.dao.MedicineEntryDAO;
import com.example.thuoc.dao.UserMedicineDAO;
import com.example.thuoc.model.Medicine;
import com.example.thuoc.model.MedicineEntry;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManagerUserMedicineEditActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineEntryAdapter adapter;
    private MemberAdapter memberAdapter;
    private final MedicineEntryDAO meDAO = new MedicineEntryDAO();
    private final MedicineDAO mDAO = new MedicineDAO();
    private final UserMedicineDAO userMedicineDAO = new UserMedicineDAO();
    private FirebaseFirestore db;
    private String usermedId;
    private String managerId;
    private String userName;
    private TextView tvTitle;
    private String selectedAvatarType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_medicine);

        managerId = getIntent().getStringExtra("managerId");
        usermedId = getIntent().getStringExtra("usermedId");
        userName = getIntent().getStringExtra("userName");

        tvTitle = findViewById(R.id.tvTitleUserMedicine);
        TextView tvGreeting = findViewById(R.id.tvGreeting); // 🔹 thêm dòng này

        if (userName != null) {
            tvTitle.setText("Danh sách thuốc");

            // 🔹 Hiển thị chào kèm tên in đậm
            String text = "Xin chào, " + userName + "!";
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    10, 10 + userName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvGreeting.setText(spannable);
        } else {
            tvGreeting.setText("Xin chào, người dùng!");
        }

        recyclerView = findViewById(R.id.recyclerViewUserMedicine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineEntryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadUserMedicines(usermedId);

        adapter.setOnItemClickListener((entry, pos) -> showMedicineDetailDialog(entry));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddMedicine).setOnClickListener(v -> showSelectMedicineDialog());
        findViewById(R.id.btnEditUser).setOnClickListener(v -> showEditUserDialog());
    }

    private void loadUserMedicines(String usermedId) {
        meDAO.getMedicinesByUserId(usermedId, list -> {
            adapter.updateData(list);
        }, e -> {
            Toast.makeText(this, "Lỗi tải thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showSelectMedicineDialog() {

        mDAO.getMedicines(
                managerId,
                (List<Medicine> medicineList) -> {
                    SelectMedicineAdapter.OnMedicineClickListener selectListener = (Medicine selectedMedicine) -> {
                        meDAO.addMedicine(usermedId, selectedMedicine,
                                () -> {
                                    Toast.makeText(this, "Đã thêm thuốc cho " + userName, Toast.LENGTH_SHORT).show();
                                    loadUserMedicines(usermedId);
                                },
                                e -> {
                                    Toast.makeText(this, "Lỗi thêm thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadUserMedicines(usermedId);
                                }
                        );
                    };

                    SelectMedicineAdapter selectAdapter = new SelectMedicineAdapter(medicineList, selectListener);

                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_medicine, null);
                    RecyclerView rv = dialogView.findViewById(R.id.rvMedicines);
                    rv.setLayoutManager(new LinearLayoutManager(this));
                    rv.setAdapter(selectAdapter);

                    TextView customTitle = new TextView(this);
                    customTitle.setText("Chọn thuốc để thêm");
                    customTitle.setPadding(40, 30, 40, 30);
                    customTitle.setTextSize(20);
                    customTitle.setTypeface(null, Typeface.BOLD);
                    customTitle.setTextColor(Color.WHITE);
                    customTitle.setBackgroundColor(Color.parseColor("#21244D"));

                    new AlertDialog.Builder(this)
                            .setCustomTitle(customTitle)
                            .setView(dialogView)
                            .setNegativeButton("Đóng", null)
                            .show();

                },
                e -> {
                    // THAM SỐ 3: Consumer<Exception> (onFailure)
                    Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ); // Kết thúc lời gọi mDAO.getMedicines
    }
    private void showMedicineDetailDialog(MedicineEntry entry) {
        if (entry == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_medicine_detail, null);
        TextView tvName = dialogView.findViewById(R.id.tvMedName);
        TextView tvExpiry = dialogView.findViewById(R.id.tvMedExpiry);
        LinearLayout llTimesContainer = dialogView.findViewById(R.id.llTimesContainer);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);

        TextView customTitle = new TextView(this);
        customTitle.setText("Chi tiết thuốc");
        customTitle.setPadding(40, 30, 40, 30);
        customTitle.setTextSize(20);
        customTitle.setTypeface(null, Typeface.BOLD);
        customTitle.setTextColor(Color.WHITE);
        customTitle.setBackgroundColor(Color.parseColor("#21244D"));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(customTitle)
                .setView(dialogView)
                .setNegativeButton("Đóng", null)
                .setNeutralButton("Xóa thuốc", null)
                .create();

        tvName.setText(entry.getName());
        tvExpiry.setText("Ngày hết hạn: " +
                (entry.getExpiryDate() != null ? entry.getExpiryDate() : "Chưa có"));

        llTimesContainer.removeAllViews();
        if (entry.getTimes() != null && !entry.getTimes().isEmpty()) {
            for (Map<String, String> t : entry.getTimes()) {
                String time = t.get("time");
                String dosage = t.get("dosage");

                LinearLayout row = new LinearLayout(this);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.setMargins(0, 4, 0, 4); // Khoảng cách giữa các hàng
                row.setLayoutParams(rowParams);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setWeightSum(10);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView timeDosageText = new TextView(this);
                timeDosageText.setTextSize(16);
                timeDosageText.setPadding(0, 4, 0, 4);

                String displayTimeDosage;
                if (dosage != null && !dosage.isEmpty()) {
                    displayTimeDosage = "• " + time + " :" + dosage ;
                } else {
                    displayTimeDosage = "• " + time;
                }
                SpannableString ss = new SpannableString(displayTimeDosage);
                int startIndex = displayTimeDosage.indexOf(time);
                int endIndex = startIndex + time.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                timeDosageText.setText(ss);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        9.0f
                );
                timeDosageText.setLayoutParams(textParams);

                ImageButton deleteButton = new ImageButton(this);
                deleteButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                deleteButton.setColorFilter(Color.RED); // Thiết lập màu đỏ
                deleteButton.setBackground(null); // Xóa nền button
                deleteButton.setPadding(16, 8, 16, 8); // Tăng padding để dễ chạm

                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                deleteButton.setLayoutParams(buttonParams);

                deleteButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa giờ uống " + time + " không?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                meDAO.deleteTime(usermedId, entry.getDocId(), time,
                                        () -> {
                                            Toast.makeText(this, "Đã xóa giờ uống: " + time, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            loadUserMedicines(usermedId);
                                        },
                                        e -> Toast.makeText(this, "Lỗi khi xóa: ", Toast.LENGTH_SHORT).show()
                                );
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });

                row.addView(timeDosageText);
                row.addView(deleteButton);
                llTimesContainer.addView(row);
            }
        } else {
            TextView noTimesText = new TextView(this);
            noTimesText.setText("Chưa có giờ uống.");
            noTimesText.setPadding(0, 16, 0, 16);
            llTimesContainer.addView(noTimesText);
        }
        btnAddTime.setOnClickListener(v -> {

            if (entry.getTimes() != null && entry.getTimes().size() >= 4) {
                new AlertDialog.Builder(this)
                        .setTitle("Giới hạn giờ uống")
                        .setMessage("Bạn đã đặt đủ 4 giờ uống cho thuốc này.\nVui lòng xóa bớt giờ cũ nếu muốn thêm mới.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // TimePicker dạng SPINNER ✔
            TimePickerDialog tpd = new TimePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,   // ép kiểu spinner
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
                                        meDAO.addTime(usermedId, entry.getDocId(), hh, dosage,
                                                () -> {
                                                    Toast.makeText(this, "Đã thêm: " + hh + " - " + dosage, Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    loadUserMedicines(usermedId);
                                                },
                                                e -> Toast.makeText(this, "Lỗi khi thêm giờ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                                    } else {
                                        Toast.makeText(this, "Chưa nhập liều lượng", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();

                    },
                    8, 0,
                    true
            );

            tpd.show();
        });

        dialog.setOnShowListener(dlg -> {
            Button btnDeleteMed = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            btnDeleteMed.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa thuốc \"" + entry.getName() + "\" không?")
                        .setPositiveButton("Xóa", (d2, w2) -> {
                            userMedicineDAO.deleteMedicine(usermedId, entry.getDocId(),
                                    () -> {
                                        Toast.makeText(this, "Đã xóa thuốc", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadUserMedicines(usermedId);
                                    },
                                    e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        SwitchMaterial switchText = dialogView.findViewById(R.id.switchTextNotify);
        SwitchMaterial switchVoice = dialogView.findViewById(R.id.switchVoiceNotify);

        UserMedicineDAO userMedicineDAO = new UserMedicineDAO();

        userMedicineDAO.getUserInfo(usermedId, user -> {
            etUserName.setText(user.getUserName() != null ? user.getUserName() : "");
            etUserPhone.setText(user.getPhone() != null ? user.getPhone() : "");
            switchText.setChecked(user.isTextNotify());
            switchVoice.setChecked(user.isVoiceNotify());
        }, e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        TextView dialogHeader = new TextView(this);
        dialogHeader.setText("Chỉnh sửa thông tin");
        dialogHeader.setPadding(40, 30, 40, 30);
        dialogHeader.setTextSize(20);
        dialogHeader.setTypeface(null, Typeface.BOLD);
        dialogHeader.setTextColor(Color.WHITE);
        dialogHeader.setBackgroundColor(Color.parseColor("#21244D"));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(dialogHeader)
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = etUserName.getText().toString().trim();
                    String newPhone = etUserPhone.getText().toString().trim();
                    boolean textNotify = switchText.isChecked();
                    boolean voiceNotify = switchVoice.isChecked();

                    userMedicineDAO.updateUserInfo(usermedId, newName, newPhone, textNotify, voiceNotify, () -> {
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
                        userMedicineDAO.deleteUser(usermedId, () -> {
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
