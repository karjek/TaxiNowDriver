package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 16/09/2016.
 */
public class NewPassengerMessage extends WebSocketMessage {

    public String gender;

    public NewPassengerMessage() {
        super(1100);
    }

    public NewPassengerMessage(long id) {
        super(id);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        super.unpack(jsonReader);

        gender = jsonReader.getString("gender");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {

        super.pack(jsonWriter);

        jsonWriter.put("gender", gender);
    }
}
