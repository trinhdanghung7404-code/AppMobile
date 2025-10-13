package com.example.thuoc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuoc.view.LoginActivity;
import com.example.thuoc.view.UserLoginActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Tạo NotificationChannel (bắt buộc Android 8+)
        createNotificationChannel();

        Button btnManager = findViewById(R.id.btnManager);
        Button btnUser = findViewById(R.id.btnUser);

        btnManager.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        });

        btnUser.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(i);
        });

    }

    /** 🔔 Tạo NotificationChannel cho AlarmReceiver */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "medicine_channel";
            String channelName = "Nhắc uống thuốc";
            String channelDesc = "Thông báo nhắc uống thuốc đúng giờ";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDesc);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
