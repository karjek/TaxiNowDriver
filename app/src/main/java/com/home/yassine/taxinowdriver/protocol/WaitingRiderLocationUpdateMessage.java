package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 23/09/2016.
 */
public class WaitingRiderLocationUpdateMessage extends WebSocketMessage {

    public String riderId;
    public double lat;
    public double lon;

    public WaitingRiderLocationUpdateMessage() {
        super(1206);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        riderId = jsonReader.getString("riderId");
        lat = jsonReader.getDouble("lat");
        lon = jsonReader.getDouble("lon");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("riderId", riderId);
        jsonWriter.put("lat", lat);
        jsonWriter.put("lon", lon);
    }
}
