package com.example.websocketclient;

import static android.system.Os.close;

import static kotlinx.coroutines.CoroutineScopeKt.cancel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {
    private WebSocketClient webSocketClient;
    private ImageView imageView;
    private static final String TAG = "MainActivity";
    private static final long RETRY_DELAY_MS = 1000; // Retry delay in milliseconds
    private boolean isConnected = false; // Flag to track connection status
    private Timer retryTimer; // Timer for managing retry attempts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageRendere);
        createWebSocketClient();
    }

    private void disconnectWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.close(); // Close the WebSocket connection
            webSocketClient = null; // Nullify to release the reference
        }
        if (retryTimer != null) {
            retryTimer.cancel(); // Stop the reconnection timer
            retryTimer = null; // Nullify to release the reference
        }
        isConnected = false; // Mark as disconnected
//        Log.d(TAG.getName(), "WebSocket connection closed and resources released.");
    }


    @Override
    protected void onDestroy() {
        disconnectWebSocket();
        super.onDestroy();

    }


    private void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://192.168.33.223:80/");
//            uri = new URI("ws://esp32camera.local:80/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }



        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.d(TAG, "onOpen");
                webSocketClient.send("Hello, World!");
            }

            @Override
            public void onTextReceived(String message) {
                Log.d(TAG, "onTextReceived: " + message);
            }

            int ct = 0;

            @Override
            public void onBinaryReceived(byte[] data) {
                Log.d(TAG, "onBinaryReceived");
                runOnUiThread(() -> {
                    if (data.length > 0) {
                        Log.d(TAG, "FPS= " + ct++);
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        imageView.setImageBitmap(bmp);
                    }
                });
                Log.d(TAG, "Data length: " + data.length);
            }

            @Override
            public void onPingReceived(byte[] data) {
                Log.d(TAG, "onPingReceived");
                isConnected = true;
                if (retryTimer != null) {
                    retryTimer.cancel();
                }
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.d(TAG, "onPongReceived");
            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, "onException: ", e);
                // Schedule reconnection attempt
                retryConnection();
            }

            @Override
            public void onCloseReceived() {
                Log.d(TAG, "onCloseReceived");
                // Schedule reconnection attempt
                retryConnection();
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.addHeader("Origin", "http://developer.example.com");
        webSocketClient.enableAutomaticReconnection(5000); // Automatic reconnection
        webSocketClient.connect();
    }

    private void retryConnection() {
        if (!isConnected) {
            if (retryTimer == null) {
                retryTimer = new Timer();
            }
            retryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "Retrying connection...");
                    createWebSocketClient();
                }
            }, RETRY_DELAY_MS);
        }
    }
}
