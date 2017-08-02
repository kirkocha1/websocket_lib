package com.kirill.kochnev.websocket;

import okhttp3.Response;

/**
 * Created by Kirill Kochnev on 24.07.17.
 */


public interface ISocketListener {

    void onMessage(String message);

    void onError(Throwable error);

    void onConnect(Response response);

    void onDisconnect(int code, String reason);

}
