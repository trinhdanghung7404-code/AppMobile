package com.example.thuoc;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.thuoc.view.LoginActivity;
import com.example.thuoc.view.UserLoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_MIC_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        // 🔹 Xin quyền mic nếu chưa có
        checkMicrophonePermission();

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

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MIC_PERMISSION
            );
        } else {
            Log.d(TAG, "🎤 Quyền micro đã được cấp");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Quyền micro đã được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Ứng dụng cần quyền micro để nhận giọng nói", Toast.LENGTH_LONG).show();
            }
        }
    }

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
