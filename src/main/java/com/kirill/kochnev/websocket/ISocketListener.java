package com.kirill.kochnev.websocket;

import okhttp3.Response;

/**
 * Created by Kirill Kochnev on 24.07.17.
 */

/**
 * this interface is a callback which {@link RxSocketWrapper} uses for delegating websocket calls
 */

public interface ISocketListener {

    /**
     *
     * @param message from real websocket
     */
    void onMessage(String message);

    void onError(Throwable error);

    void onConnect(Response response);


    /**
     *
     * @param code of disconnection reason
     * @param reason code description
     */
    void onDisconnect(int code, String reason);

}
