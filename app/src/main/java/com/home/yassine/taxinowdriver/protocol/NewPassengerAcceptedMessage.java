package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 16/09/2016.
 */
public class NewPassengerAcceptedMessage extends NewPassengerMessage {

    public String id;

    public NewPassengerAcceptedMessage() {
        super(1101);
    }
    public NewPassengerAcceptedMessage(long id) {
        super(id);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        id = jsonReader.getString("id");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("id", id);
    }
}
