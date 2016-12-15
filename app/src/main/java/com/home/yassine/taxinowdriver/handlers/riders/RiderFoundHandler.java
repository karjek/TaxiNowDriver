package com.home.yassine.taxinowdriver.handlers.riders;

import android.os.Bundle;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.RiderInfoActivity;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 20/09/2016.
 */
public class RiderFoundHandler implements IMessageHandler {

    @Override
    public void HandleMessage(MainActivity client, JSONObject jsonReader) throws JSONException {

        RiderInfoMessage riderInfo = new RiderInfoMessage();

        riderInfo.unpack(jsonReader);

        Bundle b = new Bundle();
        JSONObject jsonWriter = new JSONObject();

        riderInfo.pack(jsonWriter);
        b.putString("riderInfo", jsonWriter.toString());

        client.pushDepartureArrivalNotif(riderInfo.locAddress, riderInfo.destAddress, b);
    }
}
