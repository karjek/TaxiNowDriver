package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 17/09/2016.
 */
public class DeletePassengerMessage extends WebSocketMessage {

    public String passengerId;

    public DeletePassengerMessage() {
        super(1102);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        passengerId = jsonReader.getString("passengerId");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("passengerId", passengerId);
    }
}
