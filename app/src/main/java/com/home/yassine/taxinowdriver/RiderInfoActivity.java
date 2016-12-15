package com.home.yassine.taxinowdriver;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.home.yassine.taxinowdriver.protocol.RiderAcceptedMessage;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import com.home.yassine.taxinowdriver.protocol.RiderRefusedMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class RiderInfoActivity extends AppCompatActivity {

    public RiderInfoMessage mRiderInfo;
    public DriverApp app;

    public enum CURRENT_FRAG {
        DISPLAY_RIDER,
        SHOW_PATH
    }

    private CURRENT_FRAG current_frag;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home && current_frag == CURRENT_FRAG.SHOW_PATH) {
            getSupportFragmentManager().popBackStack();
            current_frag = CURRENT_FRAG.DISPLAY_RIDER;
            return true;
        }
        else if (item.getItemId() == android.R.id.home && current_frag == CURRENT_FRAG.DISPLAY_RIDER) {
            refused();
            return true;
        }

        return false;
    }

    private void refused() {

        JSONObject jsonWriter = new JSONObject();

        RiderRefusedMessage acceptedMessage = new RiderRefusedMessage();
        acceptedMessage.riderId = mRiderInfo.id;
        try {
            acceptedMessage.pack(jsonWriter);
            app.sendString(jsonWriter.toString());
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (current_frag == CURRENT_FRAG.DISPLAY_RIDER) {
            refused();
            super.onBackPressed();
        }
        else if (current_frag == CURRENT_FRAG.SHOW_PATH) {
            getSupportFragmentManager().popBackStack();
            current_frag = CURRENT_FRAG.DISPLAY_RIDER;
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_info);

        app = (DriverApp) getApplication();

        if (!app.isClientConnected()) {
            finish();
            return;
        }

        mRiderInfo = new RiderInfoMessage();

        String riderInfoString = getIntent().getExtras().getString("riderInfo");

        try {

            JSONObject jsonReader = new JSONObject(riderInfoString);
            mRiderInfo.unpack(jsonReader);

            DisplayRiderInfoFragment currentFrag = new DisplayRiderInfoFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frag_rider_info, currentFrag, "tag1");
            transaction.addToBackStack("remember_me");
            transaction.commit();
            current_frag = CURRENT_FRAG.DISPLAY_RIDER;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showMap() {

        ShowPathFragment currentFrag = ShowPathFragment.newInstance(
                mRiderInfo.locationLat, mRiderInfo.locationLong,
                mRiderInfo.destinationLat, mRiderInfo.destinationLong);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frag_rider_info, currentFrag, "tag2");
        transaction.addToBackStack(null);
        transaction.commit();

        current_frag = CURRENT_FRAG.SHOW_PATH;
    }

    public void accepted() {

        JSONObject jsonWriter = new JSONObject();

        RiderAcceptedMessage acceptedMessage = new RiderAcceptedMessage();
        acceptedMessage.riderId = mRiderInfo.id;
        try {
            acceptedMessage.pack(jsonWriter);
            app.sendString(jsonWriter.toString());
            app.addRiderInfo(mRiderInfo);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
