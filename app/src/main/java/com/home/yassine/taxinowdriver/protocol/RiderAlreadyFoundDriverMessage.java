package com.home.yassine.taxinowdriver.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yassine on 22/09/2016.
 */
public class RiderAlreadyFoundDriverMessage extends RiderAcceptedMessage {

    public RiderAlreadyFoundDriverMessage() {
        super(1204);
    }
}