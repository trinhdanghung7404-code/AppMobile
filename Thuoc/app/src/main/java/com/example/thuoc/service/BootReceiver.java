package com.example.thuoc.service;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import com.example.thuoc.model.MedicineEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BootReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Máy vừa khởi động lại, đang khôi phục báo thức...", Toast.LENGTH_SHORT).show();

            // 🔹 Ví dụ: Lấy lại dữ liệu từ Firestore để đặt lại alarm
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Giả sử bạn có userId lưu trong SharedPreferences
            String userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .getString("userId", null);

            if (userId != null) {
                db.collection("UserMedicine")
                        .document(userId)
                        .collection("Medicines")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                MedicineEntry med = doc.toObject(MedicineEntry.class);
                                if (med != null) {
                                    AlarmScheduler.scheduleAlarmsForMedicine(context, med, userId);
                                }
                            }
                        });
            }
        }
    }
}
