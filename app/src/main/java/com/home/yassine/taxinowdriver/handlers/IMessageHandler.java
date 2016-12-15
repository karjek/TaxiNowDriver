package com.home.yassine.taxinowdriver.handlers;

import com.home.yassine.taxinowdriver.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 15/09/2016.
 */
public interface IMessageHandler {

    void HandleMessage(MainActivity client, JSONObject jsonReader) throws JSONException;

}
