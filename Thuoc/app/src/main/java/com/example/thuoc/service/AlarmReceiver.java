package com.example.thuoc.service;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.thuoc.R;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private static TextToSpeech tts;

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");

        // Hiện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicine_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhắc uống thuốc")
                .setContentText(medicineName + " - " + dosage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build());

        // Đọc thông báo
        String speakText = "Đã đến giờ uống thuốc " + medicineName + ", liều dùng " + dosage;

        if (tts == null) {
            tts = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("vi", "VN")); // Đọc tiếng Việt
                    tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "MedicineTTS");
                }
            });
        } else {
            tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "MedicineTTS");
        }
    }
}
