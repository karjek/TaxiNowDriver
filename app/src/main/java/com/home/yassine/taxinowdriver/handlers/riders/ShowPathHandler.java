package com.home.yassine.taxinowdriver.handlers.riders;

import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.NewDestinationMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 30/09/2016.
 */
public class ShowPathHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) throws JSONException {

        final NewDestinationMessage newDestinationMessage = new NewDestinationMessage();

        newDestinationMessage.unpack(jsonReader);

        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                client.mDestFrag.drawPath(newDestinationMessage.path);
            }
        });
    }
}
