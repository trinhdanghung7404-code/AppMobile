package com.example.thuoc.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import com.example.thuoc.model.MedicineEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    public static void scheduleAlarmsForMedicine(Context context, MedicineEntry medicineEntry, String usermedId) {
        if (medicineEntry.getTimes() == null) return;

        for (Map<String, String> timeEntry : medicineEntry.getTimes()) {
            String timeStr = timeEntry.get("time");
            String dosage = timeEntry.get("dosage");

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(timeStr));

                Calendar now = Calendar.getInstance();
                cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                if (cal.before(now)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("medicineName", medicineEntry.getName());
                intent.putExtra("medicineDocId", medicineEntry.getMedicineId());
                intent.putExtra("dosage", dosage);
                intent.putExtra("usermedId", usermedId);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        (medicineEntry.getName() + timeStr).hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            cal.getTimeInMillis(),
                            pendingIntent
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "❌ Lỗi khi đặt báo thức: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
