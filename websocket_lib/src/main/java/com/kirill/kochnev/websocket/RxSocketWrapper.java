package com.kirill.kochnev.websocket;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;

/**
 * Created by Kirill Kochnev on 25.07.17.
 */

/**
 * The main purpose of this class is to wrap {@link Socket} with RxJava 2 calls for more comfortable usage
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
                subject.onNext(CONNECTED_MESSAGE);
            }

            @Override
            public void onDisconnect(int code, String reason) {
                try {
                    subject.onNext(new DisconnectResponse(reason, code).createRawJson());
                    subject.onComplete();
                } catch (Exception ex) {
                    subject.onError(ex);
                }
            }
        });
    }

    /**
     * The main method of the class
     *
     * @return Observable which emits websocket messages and errors
     */
    public Observable<String> getSocketObservable() {
        if (subject == null || subject.hasComplete() || subject.hasThrowable()) {
            subject = PublishSubject.create();
            socket.connect();
        }
        return subject;
    }

    /**
     * @return {@link Completable} which makes reconnection when on subscribe
     */
    public Completable restart() {
        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        socket.reconnect();
                    }
                });
    }

    /**
     * @return {@link Completable} which makes disconnection when on subscribe
     */
    public Completable disconnect() {
        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        socket.disconnect();
                    }
                });
    }


    /**
     * Rx wrapper for sending messages
     *
     * @param message to be send
     * @return {@link Completable}
     */
    public Completable sendMessageAsComplitable(final String message) {
            return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        if (socket.isConnected()) {
                            socket.send(message);
                        } else {
                            throw new Exception("message wasn't sended sucessfully");
                        }
                    }
                });
    }

    /**
     * Method sends message
     *
     * @param message for websocket
     */
    public void sendMessage(String message) {
        socket.send(message);
    }
}
