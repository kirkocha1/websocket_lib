package com.kirill.kochnev.websocket;

/**
 * Created by Kirill Kochnev on 24.07.17.
 */

import okhttp3.Response;

/**
 * this interface is a callback which {@link RxSocketWrapper} uses for delegating websocket calls
 */

public interface ISocketListener {

    /**
     *
     * @param message from websocket
     */
    void onMessage(String message);


    /**
     *
     * @param error from websocket
     */
    void onError(Throwable error);

    void onConnect(Response response);


    /**
     *
     * @param code of disconnection reason
     * @param reason code description
     */
    void onDisconnect(int code, String reason);

}
