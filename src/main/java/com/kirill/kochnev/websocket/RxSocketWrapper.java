package com.kirill.kochnev.websocket;

import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;

/**
 * Created by Kirill Kochnev on 25.07.17.
 */

public class RxSocketWrapper {

    private static final String TAG = "RxSocketWrapper";
    private Socket socket;
    private PublishSubject<String> subject;
    public static String CONNECTED_MESSAGE = "SOCKET_CONNECTED";

    public RxSocketWrapper(Socket socket) {
        this.socket = socket;
        this.socket.setClientListener(new ISocketListener() {
            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage: " + message);
                subject.onNext(message);
            }

            @Override
            public void onError(Throwable error) {
                if (!subject.hasThrowable()) {
                    subject.onError(error);
                }
            }

            @Override
            public void onConnect(Response response) {
                Log.d(TAG, "connected successfully: " + response.message());
                subject.onNext(CONNECTED_MESSAGE);
            }

            @Override
            public void onDisconnect(int code, String reason) {
                subject.onComplete();
                Log.d(TAG, "disconnection code: " + code + " " + reason);
            }
        });
    }

    public Observable<String> getSocketObservable() {
        Log.e(TAG, "getSocketObservable");
        if (subject == null || subject.hasComplete() || subject.hasThrowable()) {
            Log.e(TAG, "getSocketObservable subject null");
            subject = PublishSubject.create();
            socket.connect();
        }
        return subject;
    }

    public Completable restart() {
        return Completable.fromAction(() -> socket.reconnect());
    }


    public Completable disconnect() {
        return Completable.fromAction(() -> socket.disconnect());
    }

    public Completable sendMessageAsComplitable(String message) {
        Log.e(TAG, message);
        return Completable.fromAction(() -> {
            if (socket.isConnected()) {
                Log.e(TAG, "send Message " + message);
                socket.send(message);
            } else {
                throw new Exception("message wasn't sended sucessfully");
            }

        });
    }

    public void sendMessage(String message) {
        socket.send(message);
    }
}
