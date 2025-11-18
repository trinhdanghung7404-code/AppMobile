package com.example.thuoc.service;

import static android.media.AudioAttributes.*;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
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

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class VoiceListenerService extends Service {

    private static final String TAG = "VoiceListenerService";
    private static final String CHANNEL_ID = "voice_listener_channel";
    private static final String SERVER_URL = "ws://10.0.2.2:2700";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final long LISTEN_DURATION = 2 * 60 * 1000; // 2 phút

    private Handler handler;
    private boolean isListening = false;
    private boolean isRecording = false;

    private AudioRecord recorder;
    private int bufferSize;
    private VoskClient voskClient;
    private WebSocket webSocket;

    private TextToSpeech tts;

    private String usermedId, medicineDocId, dosage;

    // --- Dùng để lưu trữ dữ liệu âm thanh vào RAM ---
    private List<byte[]> audioChunks;
    // -----------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        initTTS();
        Log.d(TAG, "🎧 VoiceListenerService khởi tạo...");
    }

    private void initTTS() {
        Log.d(TAG, "🟡 Đang khởi tạo TextToSpeech...");
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("vi", "VN"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.setAudioAttributes(new Builder()
                            .setUsage(USAGE_ASSISTANT)
                            .setContentType(CONTENT_TYPE_SPEECH)
                            .build());
                }

                Log.d(TAG, "🔊 TextToSpeech đã sẵn sàng");
                Log.d(TAG, "🗣️ Engine: " + tts.getDefaultEngine());
                Log.d(TAG, "🌐 Language result: " + result);

                speak("Xin chào, tôi đang lắng nghe bạn."); // Kiểm tra phát tiếng khởi tạo
            } else {
                Log.e(TAG, "❌ Lỗi khởi tạo TTS (status=" + status + ")");
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, createNotification("Đang lắng nghe giọng nói..."));
        Log.d(TAG, "🚀 onStartCommand được gọi");

        if (!isListening) startVoskConnection();

        handler.postDelayed(() -> {
            Log.d(TAG, "⏹ Dừng sau 2 phút (tự động)");
            stopListening();
            stopSelf();
        }, LISTEN_DURATION);

        return START_STICKY;
    }

    private void startVoskConnection() {
        Log.d(TAG, "🔌 Đang kết nối tới Vosk server...");
        voskClient = new VoskClient(SERVER_URL, new WebSocketListener() {
            @RequiresPermission(Manifest.permission.RECORD_AUDIO)
            @Override
            public void onOpen(WebSocket ws, Response response) {
                webSocket = ws;
                Log.d(TAG, "🔗 Đã kết nối tới Vosk server");
                startRecording();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    String partial = json.optString("partial");
                    String finalText = json.optString("text");

                    if (!partial.isEmpty()) Log.d(TAG, "🗣️ partial: " + partial);
                    if (!finalText.isEmpty()) {
                        Log.d(TAG, "✅ final: " + finalText);
                        handleRecognitionResult(finalText.toLowerCase());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "⚠️ Parse message error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "❌ WebSocket lỗi: " + t.getMessage());
                restartListening();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "🔒 WebSocket closed: " + reason);
            }
        });
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startRecording() {
        try {
            // Khởi tạo list lưu trữ âm thanh
            audioChunks = new ArrayList<>();

            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            Log.d(TAG, "📏 bufferSize = " + bufferSize);

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "❌ AudioRecord chưa sẵn sàng!");
                return;
            }

            recorder.startRecording();
            isListening = true;
            isRecording = true;
            Log.d(TAG, "🎙️ Bắt đầu ghi âm và gửi dữ liệu...");

            new Thread(() -> {
                byte[] buffer = new byte[bufferSize];
                try {
                    while (isRecording && recorder != null) {
                        int bytesRead = recorder.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            // Gửi qua WebSocket
                            if (webSocket != null) {
                                webSocket.send(ByteString.of(buffer, 0, bytesRead));
                            }
                            // Lưu vào RAM
                            byte[] chunk = new byte[bytesRead];
                            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                            audioChunks.add(chunk);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "⚠️ Lỗi khi gửi âm thanh: " + e.getMessage());
                } finally {
                    try {
                        if (recorder != null && recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            recorder.stop();
                            Log.d(TAG, "🛑 Recorder stopped (thread finally)");
                        }
                    } catch (IllegalStateException ise) {
                        Log.w(TAG, "⚠️ Recorder stop illegal state: " + ise.getMessage());
                    }

                    if (recorder != null) {
                        recorder.release();
                        recorder = null;
                        Log.d(TAG, "💨 Recorder released (thread finally)");
                    }

                    // Phát lại sau khi dừng ghi (Phải gọi trên Main Thread)
                    handler.post(this::playRecordedAudioFromRam);
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "❌ Lỗi startRecording: " + e.getMessage());
            restartListening();
        }
    }

    private void handleRecognitionResult(String text) {
        Log.d(TAG, "📥 Nhận dạng kết quả: " + text);
        if (text.contains("đã uống") || text.contains("uống rồi")) {
            Log.i(TAG, "✅ Người dùng xác nhận đã uống thuốc");
            speak("Đã ghi nhận bạn đã uống thuốc, cảm ơn bạn!");
            if (usermedId != null && medicineDocId != null && dosage != null) {
                new MedicineDAO().subtractMedicineFromUser(usermedId, medicineDocId, dosage);
            }
            // stopListening() và stopSelf() sẽ được gọi ở đây.
            // Hàm playRecordedAudioFromRam() được gọi trong khối finally của startRecording() thread,
            // đảm bảo phát lại ngay sau khi ghi âm kết thúc.
            stopListening();
            stopSelf();
        } else if (!text.trim().isEmpty()) {
            speak("Bạn nói: " + text);
        } else {
            Log.d(TAG, "📭 Chuỗi rỗng, bỏ qua.");
        }
    }

    private void speak(String text) {
        Log.d(TAG, "🗣️ speak() gọi với nội dung: " + text);
        if (tts == null) {
            Log.e(TAG, "❌ TTS null, không thể phát!");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            Log.d(TAG, "🔈 speak() result: " + result);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // --- HÀM PHÁT LẠI TỪ RAM (Đã sửa lỗi Constructor) ---
    private void playRecordedAudioFromRam() {
        if (audioChunks == null || audioChunks.isEmpty()) {
            Log.w(TAG, "❌ Không có dữ liệu âm thanh trong RAM để phát.");
            return;
        }

        // 1. Tính toán bufferSize tối thiểu cho AudioTrack
        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AUDIO_FORMAT);

        // Đảm bảo minBufferSize hợp lệ trước khi sử dụng
        if (minBufferSize == AudioTrack.ERROR_BAD_VALUE || minBufferSize == AudioTrack.ERROR) {
            Log.e(TAG, "❌ Lỗi tính toán minBufferSize cho AudioTrack.");
            return;
        }

        // 2. Khởi tạo AudioTrack sử dụng Constructor đúng (API 23+)
        AudioTrack audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                )
                .setAudioFormat(
                        new AudioFormat.Builder()
                                .setSampleRate(SAMPLE_RATE)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();

        // Kiểm tra trạng thái khởi tạo
        if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "❌ AudioTrack không khởi tạo thành công.");
            audioTrack.release();
            return;
        }

        Log.d(TAG, "🔈 Bắt đầu phát lại âm thanh từ RAM...");
        audioTrack.play();

        // Chạy phát lại trong một luồng riêng để không chặn Main Thread
        new Thread(() -> {
            try {
                for (byte[] chunk : audioChunks) {
                    audioTrack.write(chunk, 0, chunk.length);
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Lỗi khi phát lại âm thanh từ RAM: " + e.getMessage());
            } finally {
                if (audioTrack != null) {
                    audioTrack.stop();
                    audioTrack.release();
                }
                audioChunks = null; // Giải phóng RAM sau khi phát xong
                Log.d(TAG, "✅ Đã phát xong và giải phóng RAM.");
            }
        }).start();
    }
    // ----------------------------------------------------

    private void stopListening() {
        Log.d(TAG, "🛑 stopListening() được gọi");
        try {
            isListening = false;
            isRecording = false;
            if (voskClient != null) voskClient.close();
            if (recorder != null) {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop();
                    Log.d(TAG, "📴 Recorder stopped");
                }
                recorder.release();
                recorder = null;
                Log.d(TAG, "💨 Recorder released");
            }
            Log.d(TAG, "🛑 Đã dừng ghi âm và đóng kết nối");
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Lỗi stopListening: " + e.getMessage());
        }
    }

    // Hàm restartListening được giữ nguyên
    private void restartListening() {
        Log.d(TAG, "🔁 restartListening() gọi lại sau 2s");
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
        Log.d(TAG, "💥 onDestroy() được gọi");
        super.onDestroy();
        stopListening();
        // Cần đảm bảo giải phóng audioChunks nếu chưa được giải phóng
        audioChunks = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "🧹 Đóng TextToSpeech");
        }
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "🛑 Dừng VoiceListenerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}