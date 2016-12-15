package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public class AuthenticationErrorMessage extends WebSocketMessage {

    public AuthenticationErrorMessage() {
        super(1003);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.unpack(jsonWriter);
    }
}
