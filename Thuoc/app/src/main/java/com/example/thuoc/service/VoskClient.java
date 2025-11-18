package com.example.thuoc.service;

import okhttp3.*;
import okio.ByteString;
import java.util.concurrent.TimeUnit;

public class VoskClient {

    private final OkHttpClient client;
    private WebSocket webSocket;
    private boolean isClosed = false;

    public VoskClient(String serverUrl, WebSocketListener listener) {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // keep alive for WebSockets
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder().url(serverUrl).build();
        webSocket = client.newWebSocket(request, listener != null ? listener : defaultListener());
    }

    // 🎧 Gửi mảng byte (audio PCM) tới server
    public void sendAudio(byte[] audioData, int length) {
        if (isClosed || audioData == null || length <= 0) return;
        try {
            webSocket.send(ByteString.of(audioData, 0, length));
        } catch (Exception e) {
            android.util.Log.e("VoskClient", "Send audio failed: " + e.getMessage());
        }
    }

    // 🔒 Đóng kết nối an toàn (báo hiệu trước khi close)
    public void close() {
        if (isClosed) return;
        isClosed = true;

        try {
            if (webSocket != null) {
                // Gửi tín hiệu báo kết thúc stream (tránh "connection reset by peer")
                webSocket.send("{}");
                webSocket.close(1000, "done");
                android.util.Log.d("VoskClient", "🔒 Sent end signal and closed WebSocket");
            }
        } catch (Exception e) {
            android.util.Log.e("VoskClient", "Close error: " + e.getMessage());
        } finally {
            if (client != null) {
                client.dispatcher().executorService().shutdown();
            }
        }
    }

    // 🎯 WebSocketListener mặc định (in log)
    private WebSocketListener defaultListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                android.util.Log.d("VoskClient", "✅ Connected to Vosk server");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                android.util.Log.d("VoskClient", "🗣️ Server message: " + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                android.util.Log.e("VoskClient", "❌ WebSocket failure: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                android.util.Log.d("VoskClient", "🔚 WebSocket closed: " + reason);
            }
        };
    }
}
