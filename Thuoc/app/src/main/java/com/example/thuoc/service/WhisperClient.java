package com.example.thuoc.service;

import okhttp3.*;
import okio.ByteString;
import java.util.concurrent.TimeUnit;

/**
 * Client service for connecting to the whisper.cpp WebSocket server (whisper-server.exe)
 * to perform real-time speech recognition.
 * * The whisper.cpp server expects raw PCM 16-bit, 16kHz, mono audio data.
 * * Lưu ý: Đã chuyển sang sử dụng System.out/System.err cho môi trường Java thuần (không phải Android).
 */
public class WhisperClient {

    private final OkHttpClient client;
    private WebSocket webSocket;
    private boolean isClosed = false;

    /**
     * Khởi tạo và kết nối đến server Whisper.cpp
     * @param serverUrl Địa chỉ server WebSocket (Ví dụ: ws://127.0.0.1:8080)
     * @param listener Listener để xử lý các sự kiện Open, Message, Close, Failure
     */
    public WhisperClient(String serverUrl, WebSocketListener listener) {
        // Cấu hình OkHttpClient: readTimeout = 0 là cần thiết cho streaming
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder().url(serverUrl).build();
        // Mở kết nối WebSocket
        webSocket = client.newWebSocket(request, listener != null ? listener : defaultListener());
    }

    /**
     * Gửi dữ liệu âm thanh nhị phân (PCM 16-bit, 16kHz, mono) đến server.
     * @param audioData Dữ liệu âm thanh dưới dạng mảng byte.
     */
    public boolean sendAudio(byte[] audioData) {
        if (webSocket != null && !isClosed) {
            return webSocket.send(ByteString.of(audioData, 0, audioData.length));
        }
        return false;
    }

    /**
     * Gửi tín hiệu kết thúc luồng và đóng kết nối.
     */
    public void close() {
        if (isClosed) return;
        isClosed = true;

        try {
            if (webSocket != null) {
                webSocket.close(1000, "Done");
            }
        } finally {
            client.dispatcher().executorService().shutdown();
        }
    }

    private WebSocketListener defaultListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("WhisperClient: Connected to Whisper server");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Server trả về kết quả phiên âm dưới dạng JSON string.
                System.out.println("WhisperClient Server transcription result: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Thường thì Whisper.cpp chỉ gửi kết quả dưới dạng Text (JSON).
                System.out.println("WhisperClient Server binary message received (unexpected)");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WhisperClient WebSocket failure: " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("WhisperClient WebSocket closed: " + reason);
            }
        };
    }
}