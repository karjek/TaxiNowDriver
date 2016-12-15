package com.home.yassine.taxinowdriver;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;

/**
 * Created by Yassine on 21/09/2016.
 */
public class DisplayRiderInfoFragment extends Fragment {

    private RiderInfoActivity hostActivity;

    public DisplayRiderInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        RiderInfoMessage mRiderInfo = hostActivity.mRiderInfo;

        TextView name = (TextView) getView().findViewById(R.id.riderName);
        name.setText(mRiderInfo.lastName.concat(" ").concat(mRiderInfo.firstName));

        TextView location = (TextView) getView().findViewById(R.id.riderLoc);
        location.setText(mRiderInfo.locAddress);

        TextView destination = (TextView) getView().findViewById(R.id.riderDest);
        destination.setText(mRiderInfo.destAddress);

        TextView numberRiders = (TextView) getView().findViewById(R.id.riderNumber);
        numberRiders.setText(String.valueOf(mRiderInfo.numberRiders));

        TextView detour = (TextView) getView().findViewById(R.id.riderDetour);
        detour.setText(mRiderInfo.detour ? hostActivity.getString(R.string.Yes) : hostActivity.getString(R.string.No));

        AppCompatButton showPathBtn = (AppCompatButton) getView().findViewById(R.id.showPath);

        showPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostActivity.showMap();
            }
        });

        AppCompatButton accept = (AppCompatButton) getView().findViewById(R.id.acceptRider);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostActivity.accepted();
            }
        });

        AppCompatButton cancel = (AppCompatButton) getView().findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostActivity.onBackPressed();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_rider, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        hostActivity = (RiderInfoActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hostActivity = null;
    }
}
