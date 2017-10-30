package com.kirill.kochnev.websocket;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by Kirill Kochnev on 24.07.17.
 */

/**
 * Class for which communicate directly with websocket connection {@link WebSocket}
 */
public class Socket {
    private static final String TAG = "SOCKET";
    private static int DEFAULT_RECONNECT_COUNT = 3;
    public static int NORMAL_DISCONNECTION_CODE = 4000;

    private String serverUri;
    private WebSocket ws;
    private OkHttpClient client;
    private Request request;
    private WebSocketListener listener;
    private boolean isConnected = false;
    private int reconnectAttempts;

    private ISocketListener clientListener;

    private static OkHttpClient provideDefaltOkClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public Socket(String url, OkHttpClient client, int reconnectAttempts, ISocketListener listener) {
        this.serverUri = url;
        this.reconnectAttempts = reconnectAttempts;
        this.clientListener = listener;
        this.client = client;
        initConnection();
    }

    public Socket(String url) {
        this(url, provideDefaltOkClient(), DEFAULT_RECONNECT_COUNT, null);
    }

    public Socket(String url, ISocketListener listener) {
        this(url, DEFAULT_RECONNECT_COUNT, listener);
    }

    public Socket(String url, int reconnectAttempts, ISocketListener listener) {
        this(url, provideDefaltOkClient(), reconnectAttempts, listener);
    }


    public void setClientListener(ISocketListener clientListener) {
        this.clientListener = clientListener;
    }

    private void initConnection() {
        request = new Request.Builder().url(serverUri).build();
        listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connect to socket server");
                isConnected = true;
                client.dispatcher().executorService().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (clientListener != null) {
                                    clientListener.onConnect(response);
                                }
                            }
                        });
            }

            @Override
            public void onMessage(WebSocket webSocket, String data) {
                client.dispatcher().executorService().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (clientListener != null) {
                                    clientListener.onMessage(data);
                                }
                            }
                        });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, String.format("Disconnect closing, reason is: %S, code value: %d", reason, code));
                isConnected = false;
                Log.d(TAG, "onClosing: " + reason);
                client.dispatcher().executorService().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (clientListener != null) {
                                    clientListener.onDisconnect(code, reason);
                                }
                            }
                        });
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                Log.d(TAG, "onClosed: " + reason);
                client.dispatcher().executorService().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (clientListener != null) {
                                    clientListener.onDisconnect(code, reason);
                                }
                            }
                        });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable e, Response response) {
                Log.d(TAG, "onFailed error: " + e.getMessage());
                isConnected = false;
                client.dispatcher().executorService().execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (reconnectAttempts == 0) {
                                    Log.e(TAG, "NO reconnect attempts");
                                    if (clientListener != null) {
                                        clientListener.onError(e);
                                    }
                                } else {
                                    reconnect();
                                }
                            }
                        });
            }
        };
    }

    /**
     * method which trigger reconnect
     */
    public void reconnect() {
        if (reconnectAttempts > 0) {
            ws = client.newWebSocket(request, listener);
            reconnectAttempts--;
        }
    }

    /**
     * method which trigger reconnect
     *
     * @return true if disconnected successfully
     */
    public boolean disconnect() {
        return ws.close(NORMAL_DISCONNECTION_CODE, "no listeners");
    }

    /**
     * method which makes connection with websocket if connection was established before it just returns
     */
    public void connect() {
        if (isConnected) {
            Log.d(TAG, "WEB Socket is already connected");
            return;
        }
        ws = client.newWebSocket(request, listener);
    }

    /**
     * method for sending messages
     *
     * @param message to be sended
     */
    public void send(String message) {
        ws.send(message);
    }

    public boolean isConnected() {
        return isConnected;
    }
}
