package com.example.thuoc.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresPermission;

import com.example.thuoc.model.MedicineEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmScheduler {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    public static void scheduleAlarmsForMedicine(Context context, MedicineEntry medicine) {
        if (medicine.getTimes() == null) return;

        for (String timeStr : medicine.getTimes()) {
            try {
                // Bỏ qua nếu không đúng định dạng HH:mm
                if (!timeStr.matches("\\d{2}:\\d{2}")) {
                    continue;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(timeStr));

                // Gán ngày hiện tại
                Calendar now = Calendar.getInstance();
                cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                if (cal.before(now)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("medicineName", medicine.getName());
                intent.putExtra("dosage", medicine.getDosage());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        (medicine.getName() + timeStr).hashCode(),
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

                System.out.println("Alarm set for " + medicine.getName() + " at " + timeStr);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
