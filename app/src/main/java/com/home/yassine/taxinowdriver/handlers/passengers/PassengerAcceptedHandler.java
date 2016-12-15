package com.home.yassine.taxinowdriver.handlers.passengers;

import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.Passenger;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.NewPassengerAcceptedMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 16/09/2016.
 */
public class PassengerAcceptedHandler implements IMessageHandler {
    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        NewPassengerAcceptedMessage message = new NewPassengerAcceptedMessage();

        message.unpack(jsonReader);
        final Passenger p = new Passenger();
        p.gender = message.gender;
        p.id = message.id;
        p.type =  Passenger.PassengerType.ANON;
        p.count = 1;

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.mPassengersadapter.add(p);
                client.mPassengersadapter.notifyDataSetChanged();
                client.animateNewAnonRiderHide();
            }
        });
    }
}
