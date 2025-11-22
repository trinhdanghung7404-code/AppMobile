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
import com.example.thuoc.dao.MedicineDAO;
import com.example.thuoc.service.VoskClient;

import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.util.Locale;

public class VoiceListenerService extends Service {

    private static final String TAG = "VoiceListenerService";
    private static final String CHANNEL_ID = "voice_listener_channel";
    private static final String SERVER_URL = "ws://10.24.1.168:2700";

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final long LISTEN_DURATION = 2 * 60 * 1000;

    private Handler handler;
    private boolean isListening = false;
    private boolean isRecording = false;

    private AudioRecord recorder;
    private int bufferSize;
    private WebSocket webSocket;

    private TextToSpeech tts;
    private VoskClient voskClient;

    private String usermedId, medicineDocId, dosage;

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
        startForeground(1, createNotification("Đang lắng nghe giọng nói..."));

        if (!isListening) startVoskConnection();

        handler.postDelayed(() -> {
            stopListening();
            stopSelf();
        }, LISTEN_DURATION);

        return START_STICKY;
    }

    private void startVoskConnection() {
        voskClient = new VoskClient(SERVER_URL, new WebSocketListener() {
            @RequiresPermission(Manifest.permission.RECORD_AUDIO)
            @Override
            public void onOpen(WebSocket ws, Response response) {
                webSocket = ws;
                startRecording();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    String finalText = json.optString("final");

                    if (!finalText.isEmpty()) {
                        Log.d(TAG, "final: " + finalText);
                        handleRecognitionResult(finalText.toLowerCase());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Message parse error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                restartListening();
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
                    while (isRecording && recorder != null) {
                        int bytesRead = recorder.read(buffer, 0, buffer.length);
                        if (bytesRead > 0 && webSocket != null) {
                            webSocket.send(ByteString.of(buffer, 0, bytesRead));
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

    private void handleRecognitionResult(String text) {
        if (text.contains("đã uống") || text.contains("uống rồi")) {
            speak("Đã ghi nhận bạn đã uống thuốc.");

            if (usermedId != null && medicineDocId != null && dosage != null) {
                new MedicineDAO().subtractMedicineFromUser(usermedId, medicineDocId, dosage);
            }

            stopListening();
            stopSelf();
        } else if (!text.trim().isEmpty()) {
            speak("Bạn nói: " + text);
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

        if (voskClient != null) voskClient.close();

        if (recorder != null) {
            try {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                }
            } catch (Exception ignored) {}
            recorder.release();
            recorder = null;
        }
    }

    private void restartListening() {
        handler.postDelayed(() -> {
            stopListening();
            startVoskConnection();
        }, 2000);
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
