package com.kirill.kochnev.websocket;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirill on 04.08.17.
 */


/**
 * Created when we get normal disconnection event
 */
public class DisconnectResponse {
    public static String DISCONNECTED_MESSAGE = "SOCKET_DISCONNECTED";
    public static String DISCONNECT_CODE = "code";

    @SerializedName("SOCKET_DISCONNECTED")
    private String description;

    @SerializedName("code")
    private int code;

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    public DisconnectResponse(String description, int code) {
        this.description = description;
        this.code = code;
    }

    public String createRawJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(DISCONNECTED_MESSAGE, description);
        object.put(DISCONNECT_CODE, code);
        return object.toString();
    }

}
