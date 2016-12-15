package com.home.yassine.taxinowdriver.handlers.auth;

import android.content.Intent;
import android.widget.Toast;
import com.home.yassine.taxinowdriver.LoginActivity;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public class AuthenticationErrorHandler implements IMessageHandler {

    @Override
    public void HandleMessage(final MainActivity client, JSONObject jsonReader) {

        client.app.disconnectClient();
        client.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.beginBtn.setEnabled(true);
                client.startActivity(new Intent(client, LoginActivity.class));
            }catch (Exception e) {
                if (e.getMessage() != null)
                    Toast.makeText(client, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
}