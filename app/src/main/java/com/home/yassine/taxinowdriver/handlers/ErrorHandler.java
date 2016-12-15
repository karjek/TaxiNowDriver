package com.home.yassine.taxinowdriver.handlers;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.protocol.ErrorMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 17/09/2016.
 */
public class ErrorHandler implements IMessageHandler {
    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) {

        final ErrorMessage errorMessage = new ErrorMessage();

        try {

            errorMessage.unpack(jsonReader);
            client.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(client, errorMessage.error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
