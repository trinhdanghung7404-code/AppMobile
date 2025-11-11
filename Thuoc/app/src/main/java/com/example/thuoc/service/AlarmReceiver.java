package com.example.thuoc.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.thuoc.R;
import com.example.thuoc.dao.MedicineDAO;
import com.example.thuoc.dao.UserMedicineDAO;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "medicine_channel";

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();

        // 🔹 1️⃣ Nếu người dùng nhấn "Đã uống" trong thông báo
        if ("MARK_TAKEN".equals(action)) {
            String usermedId = intent.getStringExtra("usermedId");
            String dosage = intent.getStringExtra("dosage");
            String medicineDocId = intent.getStringExtra("medicineDocId");

            if (usermedId != null && dosage != null && medicineDocId != null) {
                new MedicineDAO().subtractMedicineFromUser(usermedId, medicineDocId, dosage);
                Log.d(TAG, " Ghi nhận 'Đã uống': userMedId=" + usermedId + ", medicineDocId=" + medicineDocId + ", dosage=" + dosage);
                Toast.makeText(context, "Đã ghi nhận bạn đã uống thuốc", Toast.LENGTH_SHORT).show();

                NotificationManagerCompat.from(context).cancelAll();
            } else {
                Log.e(TAG, "Thiếu dữ liệu khi xử lý MARK_TAKEN");
                Toast.makeText(context, "Không thể ghi nhận uống thuốc: dữ liệu thiếu", Toast.LENGTH_LONG).show();
            }
            return;
        }

        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String usermedId = intent.getStringExtra("usermedId");
        String medicineDocId = intent.getStringExtra("medicineDocId");

        if (usermedId == null || medicineDocId == null) {
            Log.e(TAG, "Thiếu usermedId hoặc medicineDocId khi tạo thông báo");
            Toast.makeText(context, "Không thể gửi thông báo: thiếu thông tin người dùng hoặc thuốc", Toast.LENGTH_LONG).show();
            return;
        }

        createNotificationChannel(context);

        UserMedicineDAO usermedDAO = new UserMedicineDAO();
        usermedDAO.getNotificationSettings(usermedId, (textNotify, voiceNotify) -> {
            Log.d(TAG, "📣 Thông báo - text=" + textNotify + ", voice=" + voiceNotify);

            if (!textNotify && !voiceNotify) {
                Log.d(TAG, "Người dùng tắt hết thông báo");
                return;
            }

            String message = medicineName + " - " + dosage;

            // 🔹 Hiển thị thông báo văn bản
            if (textNotify) {
                Intent takenIntent = new Intent(context, AlarmReceiver.class);
                takenIntent.setAction("MARK_TAKEN");
                takenIntent.putExtra("usermedId", usermedId);
                takenIntent.putExtra("medicineDocId", medicineDocId);
                takenIntent.putExtra("dosage", dosage);

                PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (int) System.currentTimeMillis(),
                        takenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("💊 Nhắc uống thuốc")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .addAction(android.R.drawable.ic_input_add, "Đã uống", takenPendingIntent);

                NotificationManagerCompat.from(context)
                        .notify((int) System.currentTimeMillis(), builder.build());
            }

            // 🔹 Kích hoạt service lắng nghe giọng nói trong 10 phút
            if (voiceNotify) {
                try {
                    Intent micIntent = new Intent(context, VoiceListenerService.class);
                    micIntent.putExtra("usermedId", usermedId);
                    micIntent.putExtra("medicineDocId", medicineDocId);
                    micIntent.putExtra("dosage", dosage);
                    micIntent.putExtra("duration", 10 * 60 * 1000); // ⏱ 10 phút

                    ContextCompat.startForegroundService(context, micIntent);
                    Log.d(TAG, "🎤 VoiceListenerService được bật trong 10 phút");

                } catch (Exception e) {
                    Log.e(TAG, "🔥 Lỗi khi bật VoiceListenerService: " + e.getMessage());
                }
            }

        }, e -> {
            Log.e(TAG, "🔥 Lỗi khi đọc cài đặt người dùng: " + e.getMessage());
            Toast.makeText(context, "❌ Lỗi khi đọc cài đặt thông báo", Toast.LENGTH_LONG).show();
        });
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc uống thuốc",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc uống thuốc đúng giờ");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
