package com.example.thuoc.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;

import com.example.thuoc.R;
// Đổi tên import từ VoskClient sang WhisperClient
import com.example.thuoc.service.WhisperClient;

import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.Locale;

public class VoiceListenerService extends Service {

    private static final String TAG = "VoiceListenerService";
    private static final String CHANNEL_ID = "voice_listener_channel";

    // Cập nhật URL và PORT.
    // Nếu chạy trên Android Emulator, hãy dùng 10.0.2.2
    // Nếu chạy trên thiết bị vật lý, hãy dùng IP máy tính của bạn (10.24.1.168)

    private static final String SERVER_URL = "ws://10.0.2.2:8080";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final long LISTEN_DURATION = 2 * 60 * 1000;

    private Handler handler;
    private boolean isListening = false;
    private boolean isRecording = false;
    private boolean isConfirmed = false;
    private AudioRecord recorder;
    private int bufferSize;
    private WebSocket webSocket;

    private TextToSpeech tts;
    // Đổi tên biến từ voskClient sang whisperClient
    private WhisperClient whisperClient;

    private String usermedId, userId, medicineDocId, dosage, medicineName;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        initTTS();
        Log.d(TAG, "Service created");
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("vi", "VN"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                    .build()
                    );
                }
                speak("Xin chào, tôi đang lắng nghe bạn.");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            usermedId = intent.getStringExtra("usermedId");
            userId = intent.getStringExtra("userId");
            medicineDocId = intent.getStringExtra("medicineDocId");
            dosage = intent.getStringExtra("dosage");
            medicineName = intent.getStringExtra("medicineName");
        }

        Log.d(TAG, "📦 VoiceService data: "
                + "usermedId=" + usermedId
                + ", userId=" + userId
                + ", medicineDocId=" + medicineDocId
                + ", dosage=" + dosage);


        startForeground(1, createNotification("Đang lắng nghe giọng nói..."));

        if (!isListening) startWhisperConnection();

        handler.postDelayed(() -> {

            if (!isConfirmed) {
                Log.d(TAG, "⏰ Voice timeout – sending MARK_MISSED");
                sendMissed();
            }

            stopListening();
            stopSelf();

        }, LISTEN_DURATION);

        return START_STICKY;
    }

    private void sendMissed() {
        Intent missIntent = new Intent(this, AlarmReceiver.class);
        missIntent.setAction("MARK_MISSED");
        missIntent.putExtra("usermedId", usermedId);
        missIntent.putExtra("userId", userId);
        missIntent.putExtra("medicineDocId", medicineDocId);
        missIntent.putExtra("dosage", dosage);
        missIntent.putExtra("medicineName", medicineName);
        missIntent.putExtra("method", "VOICE_TIMEOUT");
        sendBroadcast(missIntent);
    }

    private void sendMarkTaken() {
        Intent confirmIntent = new Intent(this, AlarmReceiver.class);
        confirmIntent.setAction("MARK_TAKEN");
        confirmIntent.putExtra("usermedId", usermedId);
        confirmIntent.putExtra("userId", userId);
        confirmIntent.putExtra("medicineDocId", medicineDocId);
        confirmIntent.putExtra("dosage", dosage);
        confirmIntent.putExtra("medicineName", medicineName);
        confirmIntent.putExtra("method", "VOICE");

        sendBroadcast(confirmIntent);
    }

    private void startWhisperConnection() {
        whisperClient = new WhisperClient(SERVER_URL, new WebSocketListener() {
            @RequiresPermission(Manifest.permission.RECORD_AUDIO)
            @Override
            public void onOpen(WebSocket ws, Response response) {
                webSocket = ws;
                startRecording();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    JSONObject json = new JSONObject(text);

                    String intent = json.optString("intent", "");
                    boolean done = json.optBoolean("done", false);

                    if ("CONFIRM_TAKEN_MEDICINE".equals(intent) || done) {

                        isConfirmed = true; // ⭐ CỰC KỲ QUAN TRỌNG

                        Log.d(TAG, "✅ Intent matched – handling confirmation");

                        isRecording = false;
                        isListening = false;

                        if (whisperClient != null) {
                            whisperClient.close();
                            whisperClient = null;
                        }

                        speak("Đã ghi nhận bạn đã uống thuốc.");

                        sendMarkTaken();

                        stopSelf();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Message parse error", e);
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                if (!isListening) {
                    Log.d(TAG, "WS closed normally – not restarting");
                    return;
                }
                Log.e(TAG, "WebSocket failure", t);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed");
            }
        });
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startRecording() {
        try {
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                return;
            }

            recorder.startRecording();
            isListening = true;
            isRecording = true;

            new Thread(() -> {
                byte[] buffer = new byte[bufferSize];

                try {
                    while (isRecording && recorder != null && whisperClient != null) {
                        int bytesRead = recorder.read(buffer, 0, buffer.length);

                        if (bytesRead > 0 && !whisperClient.sendAudio(buffer)) {
                            Log.d(TAG, "WS closed – stopping audio thread");
                            break;
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    try {
                        if (recorder != null && recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            recorder.stop();
                        }
                    } catch (Exception ignored) {
                    }

                    if (recorder != null) {
                        recorder.release();
                        recorder = null;
                    }
                }
            }).start();

        } catch (Exception e) {
            restartListening();
        }
    }

    private void speak(String text) {
        if (tts == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void stopListening() {
        isListening = false;
        isRecording = false;

        if (whisperClient != null) {
            whisperClient.close();
            whisperClient = null;
        }

        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception ignored) {}
            recorder.release();
            recorder = null;
        }
    }

    private void restartListening() {
        handler.postDelayed(() -> {
            if (isListening) {
                Log.d(TAG, "⏰ Voice timeout → MARK_MISSED");
                sendMissed();
            }
            stopListening();
            stopSelf();
        }, LISTEN_DURATION);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Voice Listener",
                    NotificationManager.IMPORTANCE_LOW
            );
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
                .build();
    }

    @Override
    public void onDestroy() {
        stopListening();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}