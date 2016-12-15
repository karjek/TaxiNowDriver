package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 21/09/2016.
 */
public class RiderInfoMessage extends WebSocketMessage {

    public String id;
    public String firstName;
    public String lastName;

    public String locAddress;
    public double locationLat;
    public double locationLong;

    public String destAddress;
    public double destinationLat;
    public double destinationLong;

    public int numberRiders;
    public boolean detour;
    public String gender;

    public RiderInfoMessage() {
        super(1200);
    }

    @Override
    public void unpack(JSONObject jsonReader) throws JSONException {
        super.unpack(jsonReader);

        id = jsonReader.getString("id");
        firstName = jsonReader.getString("firstName");
        lastName = jsonReader.getString("lastName");

        locAddress = jsonReader.getString("locAddress");
        locationLat = jsonReader.getDouble("locationLat");
        locationLong = jsonReader.getDouble("locationLong");

        destAddress = jsonReader.getString("destAddress");
        destinationLat = jsonReader.getDouble("destinationLat");
        destinationLong = jsonReader.getDouble("destinationLong");

        numberRiders = jsonReader.getInt("numberRider");
        detour = jsonReader.getBoolean("detour");
        gender = jsonReader.getString("gender");
    }

    @Override
    public void pack(JSONObject jsonWriter) throws JSONException {
        super.pack(jsonWriter);

        jsonWriter.put("id", id);
        jsonWriter.put("firstName", firstName);
        jsonWriter.put("lastName", lastName);

        jsonWriter.put("locAddress", locAddress);
        jsonWriter.put("locationLat", locationLat);
        jsonWriter.put("locationLong", locationLong);

        jsonWriter.put("destAddress", destAddress);
        jsonWriter.put("destinationLat", destinationLat);
        jsonWriter.put("destinationLong", destinationLong);

        jsonWriter.put("numberRider", numberRiders);
        jsonWriter.put("detour", detour);
        jsonWriter.put("gender", gender);
    }
}
