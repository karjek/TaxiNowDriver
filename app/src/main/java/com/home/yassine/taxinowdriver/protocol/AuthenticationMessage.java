package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public class AuthenticationMessage extends WebSocketMessage {

    public String email;
    public String token;
    public String manufacturer;
    public String model;

    public AuthenticationMessage() {
        super(1001);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {

        super.unpack(jsonReader);

        email = jsonReader.getString("email");
        token = jsonReader.getString("token");
        manufacturer = jsonReader.getString("manufacturer");
        model = jsonReader.getString("model");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {

        super.pack(jsonWriter);

        jsonWriter.put("email", email);
        jsonWriter.put("token", token);
        jsonWriter.put("manufacturer", manufacturer);
        jsonWriter.put("model", model);
    }
}
