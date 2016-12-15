package com.home.yassine.taxinowdriver.handlers.riders;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.R;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.RiderCanceledRequestMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 * Called when driver receive notification,
 * but when accept, find out that the rider already canceled
 */
public class RiderCanceledRequestHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        RiderCanceledRequestMessage message = new RiderCanceledRequestMessage();

        message.unpack(jsonReader);

        client.app.removeRiderInfo(message.riderId);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(client, R.string.client_canceled, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
