package com.home.yassine.taxinowdriver.handlers.riders;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.Passenger;
import com.home.yassine.taxinowdriver.R;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.RiderCanceledRequestMessage;
import com.home.yassine.taxinowdriver.protocol.WaitingRiderDisconnectedMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 23/09/2016.
 */
public class WaitingRiderDisconnectedHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        final WaitingRiderDisconnectedMessage message = new WaitingRiderDisconnectedMessage();

        message.unpack(jsonReader);

        client.app.removeRiderInfo(message.riderId);

        for (int i = 0; i < client.mPassengersadapter.getCount(); i++) {

            final Passenger p = client.mPassengersadapter.getItem(i);

            if (p.id.equals(message.riderId) && p.type == Passenger.PassengerType.CLIENT) {
                client.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        client.mPassengersadapter.remove(p);
                        client.mPassengersadapter.notifyDataSetChanged();
                        client.closeWaitingRiderMap(message.riderId);
                        Toast.makeText(client, R.string.client_canceled, Toast.LENGTH_SHORT).show();

                    }
                });

                break;
            }
        }
    }
}
