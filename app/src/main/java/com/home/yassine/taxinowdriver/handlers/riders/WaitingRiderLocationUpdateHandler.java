package com.home.yassine.taxinowdriver.handlers.riders;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.Passenger;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import com.home.yassine.taxinowdriver.protocol.WaitingRiderLocationUpdateMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 23/09/2016.
 */
public class WaitingRiderLocationUpdateHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        final WaitingRiderLocationUpdateMessage message = new WaitingRiderLocationUpdateMessage();

        message.unpack(jsonReader);

        boolean exists = false;

        for (int i = 0; i < client.mPassengersadapter.getCount(); i++) {

            final Passenger p = client.mPassengersadapter.getItem(i);

            if (p.type == Passenger.PassengerType.CLIENT && p.id.equals(message.riderId)) {
                exists = true;
                break;
            }
        }

        if (!exists)
            return;

        final RiderInfoMessage riderInfoMessage = client.app.getRiderInfo(message.riderId);

        if (riderInfoMessage == null)
            return;

        riderInfoMessage.locationLat = message.lat;
        riderInfoMessage.locationLong = message.lon;

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.UpdateRiderPosition(riderInfoMessage.id, message.lat, message.lon);
            }
        });
    }
}
