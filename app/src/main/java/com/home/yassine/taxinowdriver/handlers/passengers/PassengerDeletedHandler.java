package com.home.yassine.taxinowdriver.handlers.passengers;

import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.Passenger;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.PassengerDeletedMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 17/09/2016.
 */
public class PassengerDeletedHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        PassengerDeletedMessage message = new PassengerDeletedMessage();

        message.unpack(jsonReader);

        for (int i = 0; i < client.mPassengersadapter.getCount(); i++) {
            final Passenger p = client.mPassengersadapter.getItem(i);
            if (p.id.equals(message.id) && message.type == (p.type == Passenger.PassengerType.ANON ? 0 : 1)) {
                client.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        client.mPassengersadapter.remove(p);
                        client.mPassengersadapter.notifyDataSetChanged();
                    }
                });

                break;
            }
        }
    }
}
