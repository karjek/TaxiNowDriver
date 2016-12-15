package com.home.yassine.taxinowdriver.protocol;

import android.util.Log;
import com.home.yassine.taxinowdriver.MainActivity;
import com.home.yassine.taxinowdriver.handlers.ErrorHandler;
import com.home.yassine.taxinowdriver.handlers.IMessageHandler;
import com.home.yassine.taxinowdriver.handlers.auth.AuthenticationErrorHandler;
import com.home.yassine.taxinowdriver.handlers.auth.AuthenticationHandler;
import com.home.yassine.taxinowdriver.handlers.passengers.PassengerAcceptedHandler;
import com.home.yassine.taxinowdriver.handlers.passengers.PassengerDeletedHandler;
import com.home.yassine.taxinowdriver.handlers.riders.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Yassine on 15/09/2016.
 */
public class MessageReceiver {

    private HashMap<Long, IMessageHandler> Handlers = new HashMap<>();

    public MessageReceiver() {

        Handlers.put((long) 1000, new ErrorHandler());
        Handlers.put((long) 1002, new AuthenticationHandler());
        Handlers.put((long) 1003, new AuthenticationErrorHandler());
        Handlers.put((long) 1101, new PassengerAcceptedHandler());
        Handlers.put((long) 1103, new PassengerDeletedHandler());
        Handlers.put((long) 1105, new RiderDeletedMessageHandler());
        Handlers.put((long) 1200, new RiderFoundHandler());
        Handlers.put((long) 1203, new NewPassengerRiderHandler());
        Handlers.put((long) 1204, new RiderAlreadyFoundDriverHandler());
        Handlers.put((long) 1205, new RiderCanceledRequestHandler());
        Handlers.put((long) 1206, new WaitingRiderLocationUpdateHandler());
        Handlers.put((long) 1400, new WaitingRiderDisconnectedHandler());
        Handlers.put((long) 1500, new ShowPathHandler());
    }

    public void Receive(MainActivity activity, String message) {

        try {
            JSONObject jo = new JSONObject(message);
            long messageId = jo.getLong("messageId");
            if (Handlers.containsKey(messageId))
                Handlers.get(messageId).HandleMessage(activity, jo);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PARSE_JSON_ERROR", message);
            Log.e("error : ", e.getMessage());
        }
    }
}