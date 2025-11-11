package com.example.thuoc.service;

import okhttp3.*;
import okio.ByteString;
import java.util.concurrent.TimeUnit;

public class VoskClient {

    private final OkHttpClient client;
    private final WebSocket webSocket;

    public VoskClient(String serverUrl, WebSocketListener listener) {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // keep alive for websockets
                .build();

        Request request = new Request.Builder().url(serverUrl).build();

        webSocket = client.newWebSocket(request, listener != null ? listener : defaultListener());
    }

    // Gửi mảng byte (audio PCM) tới server
    public void sendAudio(byte[] audioData, int length) {
        if (audioData == null || length <= 0) return;
        webSocket.send(ByteString.of(audioData, 0, length));
    }

    // Đóng kết nối
    public void close() {
        try {
            webSocket.close(1000, "done");
        } finally {
            client.dispatcher().executorService().shutdown();
        }
    }

    // Một WebSocketListener mặc định (in log)
    private WebSocketListener defaultListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                android.util.Log.d("VoskClient", "Connected to Vosk server");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                android.util.Log.d("VoskClient", "Server: " + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                android.util.Log.e("VoskClient", "WebSocket failure", t);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                android.util.Log.d("VoskClient", "WebSocket closed: " + reason);
            }
        };
    }
}
