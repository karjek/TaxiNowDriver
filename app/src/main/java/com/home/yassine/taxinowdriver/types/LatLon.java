package com.home.yassine.taxinowdriver.types;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 30/09/2016.
 */
public class LatLon implements INetworkType {
    public double lat;
    public double lon;

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        lon = jsonReader.getDouble("lon");
        lat = jsonReader.getDouble("lat");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        jsonWriter.put("lat", lat);
        jsonWriter.put("lon", lon);
    }
}
