package com.home.yassine.taxinowdriver.handlers.riders;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.R;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.RiderAlreadyFoundDriverMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class RiderAlreadyFoundDriverHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        RiderAlreadyFoundDriverMessage message = new RiderAlreadyFoundDriverMessage();

        message.unpack(jsonReader);

        client.app.removeRiderInfo(message.riderId);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(client, R.string.taxi_already_found, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
