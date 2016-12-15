package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class RiderAcceptedMessage extends WebSocketMessage {

    public String riderId;


    public RiderAcceptedMessage() {
        super(1201);
    }

    public RiderAcceptedMessage(long messageId) {
        super(messageId);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        riderId = jsonReader.getString("riderId");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("riderId", riderId);
    }
}