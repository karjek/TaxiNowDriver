package com.home.yassine.taxinowdriver.handlers.auth;

import android.widget.Toast;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.AuthenticatedMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public class AuthenticationHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity activity, JSONObject jsonReader) throws JSONException {

        AuthenticatedMessage authMessage = new AuthenticatedMessage();

        authMessage.unpack(jsonReader);
        activity.app.setClientConnected();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    activity.beginBtn.setEnabled(true);
                    activity.requestLocationUpdates();
                    activity.animateShow();
                }catch (Exception e) {
                    if (e.getMessage() != null)
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
