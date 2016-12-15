package com.home.yassine.taxinowdriver.handlers.riders;

import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.Passenger;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.NewPassengerRiderMessage;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class NewPassengerRiderHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        NewPassengerRiderMessage message = new NewPassengerRiderMessage();

        message.unpack(jsonReader);

        RiderInfoMessage riderInfo = client.app.getRiderInfo(message.riderId);

        final Passenger p = new Passenger();
        p.gender = riderInfo.gender;
        p.id = riderInfo.id;
        p.type = Passenger.PassengerType.CLIENT;
        p.iconName = String.valueOf(riderInfo.lastName.charAt(0)) + String.valueOf(riderInfo.firstName.charAt(0)) + (riderInfo.gender.equals("F") ? "p" : "b");
        p.iconName = p.iconName.toLowerCase();
        p.count = riderInfo.numberRiders;

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.mPassengersadapter.add(p);
                client.mPassengersadapter.notifyDataSetChanged();
                client.animateNewAnonRiderHide();
                client.mDestFrag.refreshRiderList();
            }
        });
    }
}
