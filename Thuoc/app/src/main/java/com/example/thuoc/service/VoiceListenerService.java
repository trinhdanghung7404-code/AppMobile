package com.example.thuoc.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.thuoc.R;
import com.example.thuoc.dao.MedicineDAO;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VoiceListenerService extends Service {

    private static final String TAG = "VoiceListenerService";
    private static final String CHANNEL_ID = "voice_listener_channel";

    private Handler handler;
    private boolean isListening = false;

    private String usermedId, medicineDocId, dosage;

    private static final long LISTEN_DURATION = 2 * 60 * 1000; // 2 phút
    private static final long RESTART_DELAY = 2000; // 2 giây

    // Server nhận dạng giọng nói (chạy Flask + Vosk)
    private static final String SERVER_URL = "http://192.168.1.10:5000/recognize";

    // Cấu hình AudioRecord
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder;
    private int bufferSize;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        Log.d(TAG, "🎧 VoiceListenerService khởi tạo...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification("Đang lắng nghe giọng nói..."));

        if (!isListening) {
            startRecording();
        }

        // Dừng sau 2 phút
        handler.postDelayed(() -> {
            Log.d(TAG, "⏹ Dừng sau 2 phút");
            stopRecording();
            stopSelf();
        }, LISTEN_DURATION);

        return START_STICKY;
    }

    private void startRecording() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

            recorder.startRecording();
            isListening = true;
            Log.d(TAG, "🎙️ Bắt đầu ghi âm...");

            new Thread(this::processAudioStream).start();
        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi startRecording: " + e.getMessage());
            restartListening();
        }
    }

    private void processAudioStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[bufferSize];

        try {
            long startTime = System.currentTimeMillis();

            while (isListening && System.currentTimeMillis() - startTime < 15000) { // ghi 15s mỗi lượt
                int read = recorder.read(buffer, 0, buffer.length);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                }
            }

            Log.d(TAG, "📦 Gửi âm thanh lên server...");
            String result = sendToServer(outputStream.toByteArray());
            Log.d(TAG, "🗣️ Kết quả nhận dạng: " + result);

            if (result != null && (result.contains("đã uống") || result.contains("uống rồi"))) {
                Log.i(TAG, "✅ Người dùng xác nhận đã uống thuốc");
                if (usermedId != null && medicineDocId != null && dosage != null) {
                    new MedicineDAO().subtractMedicineFromUser(usermedId, medicineDocId, dosage);
                }
                stopSelf();
            } else {
                Log.d(TAG, "🔁 Không khớp kết quả, ghi lại sau " + RESTART_DELAY + "ms");
                restartListening();
            }

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Lỗi processAudioStream: " + e.getMessage());
            restartListening();
        }
    }

    private String sendToServer(byte[] audioData) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "audio/wav");

            OutputStream os = conn.getOutputStream();
            os.write(audioData);
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            JSONObject json = new JSONObject(response.toString());
            return json.optString("text", "").toLowerCase();

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi sendToServer: " + e.getMessage());
            return null;
        }
    }

    private void stopRecording() {
        try {
            isListening = false;
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Lỗi stopRecording: " + e.getMessage());
        }
    }

    private void restartListening() {
        handler.postDelayed(() -> {
            stopRecording();
            startRecording();
        }, RESTART_DELAY);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Voice Listener",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Lắng nghe giọng nói để xác nhận uống thuốc");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String message) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Lắng nghe người dùng")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_mic)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "🛑 Dừng VoiceListenerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
