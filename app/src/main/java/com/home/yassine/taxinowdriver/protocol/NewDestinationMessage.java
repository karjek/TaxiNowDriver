package com.home.yassine.taxinowdriver.protocol;

import com.google.android.gms.maps.model.LatLng;
import com.home.yassine.taxinowdriver.types.LatLon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yassine on 28/09/2016.
 */
public class NewDestinationMessage extends WebSocketMessage {

    public ArrayList<LatLon> path;

    public NewDestinationMessage() {
        super(1500);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.pack(jsonReader);

        path = new ArrayList<>();

        JSONArray array = jsonReader.getJSONArray("path");

        for (int i = 0; i < array.length(); i++) {
            JSONObject jo = (JSONObject) array.get(i);
            LatLon latLon = new LatLon();
            latLon.lat = jo.getDouble("lat");
            latLon.lon = jo.getDouble("lon");
            path.add(latLon);
        }
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        JSONArray jsonPath = new JSONArray();

        for (LatLon latLon : path) {

            JSONObject jsonObject = new JSONObject();
            latLon.pack(jsonObject);
            jsonPath.put(jsonObject);
        }

        jsonWriter.put("path", jsonPath);
    }
}