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
import com.example.thuoc.dao.MedicationLogDAO;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "medicine_channel";
    private static final String ACTION_MARK_TAKEN = "MARK_TAKEN";
    private static final String ACTION_MARK_MISSED = "MARK_MISSED";

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();

        if (ACTION_MARK_TAKEN.equals(action)) {
            handleMarkTaken(context, intent);
            return;
        }

        if (ACTION_MARK_MISSED.equals(action)) {
            handleMarkMissed(context, intent);
            return;
        }

        handleAlarmNotify(context, intent);
    }

    // ===================== MARK TAKEN =====================
    private void handleMarkTaken(Context context, Intent intent) {
        String usermedId = intent.getStringExtra("usermedId");
        String userId = intent.getStringExtra("userId");              // ✅
        String medicineDocId = intent.getStringExtra("medicineDocId");
        String dosage = intent.getStringExtra("dosage");
        String medicineName = intent.getStringExtra("medicineName");
        String method = intent.getStringExtra("method");

        if (usermedId == null || userId == null || medicineDocId == null || dosage == null) {
            Log.e(TAG, "❌ MARK_TAKEN thiếu dữ liệu");
            Toast.makeText(context, "Không thể ghi nhận uống thuốc", Toast.LENGTH_LONG).show();
            return;
        }

        // Trừ thuốc
        new MedicineDAO().subtractMedicineFromUser(usermedId, medicineDocId, dosage);

        // Ghi log
        new MedicationLogDAO().logEvent(
                usermedId,
                userId,                     // ✅ ĐÚNG userId
                medicineName,
                dosage,
                "TAKEN",
                method != null ? method : "UNKNOWN"
        );

        Log.d(TAG, "✅ Đã trừ thuốc + ghi log");

        Toast.makeText(context, "✅ Đã ghi nhận bạn đã uống thuốc", Toast.LENGTH_SHORT).show();

        NotificationManagerCompat.from(context).cancelAll();
        context.stopService(new Intent(context, VoiceListenerService.class));
    }

    // ===================== MARK MISSED =====================
    private void handleMarkMissed(Context context, Intent intent) {
        String usermedId = intent.getStringExtra("usermedId");
        String userId = intent.getStringExtra("userId");              // ✅
        String dosage = intent.getStringExtra("dosage");
        String medicineName = intent.getStringExtra("medicineName");

        if (usermedId == null || userId == null) {
            Log.e(TAG, "❌ MARK_MISSED thiếu dữ liệu");
            return;
        }

        new MedicationLogDAO().logEvent(
                usermedId,
                userId,                     // ✅ ĐÚNG userId
                medicineName,
                dosage,
                "MISSED",
                "VOICE_TIMEOUT"
        );

        Log.d(TAG, "❌ Đã ghi nhận QUÊN uống thuốc");
    }

    // ===================== ALARM =====================
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void handleAlarmNotify(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String usermedId = intent.getStringExtra("usermedId");
        String userId = intent.getStringExtra("userId");              // ✅
        String medicineDocId = intent.getStringExtra("medicineDocId");

        if (usermedId == null || userId == null || medicineDocId == null) {
            Log.e(TAG, "❌ Alarm thiếu dữ liệu");
            return;
        }

        createNotificationChannel(context);

        UserMedicineDAO dao = new UserMedicineDAO();
        dao.getNotificationSettings(usermedId, (textNotify, voiceNotify) -> {

            if (!textNotify && !voiceNotify) return;

            String message = medicineName + " - " + dosage;

            // ================= TEXT =================
            if (textNotify) {
                Intent takenIntent = new Intent(context, AlarmReceiver.class);
                takenIntent.setAction(ACTION_MARK_TAKEN);
                takenIntent.putExtra("usermedId", usermedId);
                takenIntent.putExtra("userId", userId);              // ✅
                takenIntent.putExtra("medicineDocId", medicineDocId);
                takenIntent.putExtra("dosage", dosage);
                takenIntent.putExtra("medicineName", medicineName);
                takenIntent.putExtra("method", "BUTTON");

                PendingIntent takenPI = PendingIntent.getBroadcast(
                        context,
                        0,
                        takenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("💊 Nhắc uống thuốc")
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .addAction(
                                        android.R.drawable.checkbox_on_background,
                                        "Đã uống",
                                        takenPI
                                );

                NotificationManagerCompat.from(context)
                        .notify((int) System.currentTimeMillis(), builder.build());
            }

            // ================= VOICE =================
            if (voiceNotify) {
                Intent voiceIntent = new Intent(context, VoiceListenerService.class);
                voiceIntent.putExtra("usermedId", usermedId);
                voiceIntent.putExtra("userId", userId);              // ✅
                voiceIntent.putExtra("medicineDocId", medicineDocId);
                voiceIntent.putExtra("dosage", dosage);
                voiceIntent.putExtra("medicineName", medicineName);
                voiceIntent.putExtra("duration", 10 * 60 * 1000);

                ContextCompat.startForegroundService(context, voiceIntent);
                Log.d(TAG, "🎤 VoiceListenerService started (10 phút)");
            }

        }, e -> Log.e(TAG, "🔥 Lỗi đọc setting: " + e.getMessage()));
    }

    // ===================== CHANNEL =====================
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc uống thuốc",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc uống thuốc");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
