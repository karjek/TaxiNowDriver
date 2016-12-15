package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 17/09/2016.
 */
public class PassengerDeletedMessage extends WebSocketMessage {

    public String id;
    public int type;

    public PassengerDeletedMessage() {
        super(1103);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        id = jsonReader.getString("id");
        type = jsonReader.getInt("type");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("id", id);
        jsonWriter.put("type", type);
    }
}
