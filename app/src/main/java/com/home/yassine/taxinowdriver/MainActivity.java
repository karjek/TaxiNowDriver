package com.home.yassine.taxinowdriver;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.home.yassine.taxinowdriver.protocol.*;
import com.wang.avi.AVLoadingIndicatorView;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DriverApp.IConnectionHandler {

    private static final long LOCATION_REFRESH_TIME = 2000;
    public Button beginBtn;
    Button cancelBtn;
    Button newRiderBtn;
    TextView mLogo;
    RelativeLayout mNewRiderInterface;

    AVLoadingIndicatorView loader;
    MainActivity context = this;
    public User mUser;
    private MessageReceiver mMessageReceiver;
    public PassengersAdapter mPassengersadapter;
    private int mNotificationId = 0;
    ListView passengerList;
    Location currentLocation;

    public DriverApp app;
    private SupportMapFragment _frag;
    public boolean showingMap = false;
    private GoogleMap mMap;
    private Marker Mmarker;
    private String currentRiderIdOnMap = null;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Button mDestinationBtn;
    public DestinationFragment mDestFrag;
    private boolean showingDestMap = false;
    public String mCurrentCity;

    @Override
    public void onBackPressed() {

        if (showingMap) {
            showingMap = false;
            currentRiderIdOnMap = null;
            getSupportFragmentManager().beginTransaction().hide(_frag).commitAllowingStateLoss();
        }

        // verify hostActivity exists because of a bug that cause crash
        if (showingDestMap && mDestFrag.hostActivity != null) {
            showingDestMap = false;
            getSupportFragmentManager().beginTransaction().hide(mDestFrag).commitAllowingStateLoss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Log.e("create", "created main");

            app = (DriverApp) getApplication();

            if (getSupportActionBar() != null)
                getSupportActionBar().hide();

            mUser = Storage.LoadUser(this);

            if (mUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
            }

            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }


        beginBtn = (Button) findViewById(R.id.begin_btn);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUser == null) {
                    startActivity(new Intent(context, LoginActivity.class));
                }
                else {
                    beginBtn.setEnabled(false);
                    context.connectWebSocket();
                }
            }
        });

        cancelBtn = (Button) findViewById(R.id.cancel_btn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app.disconnectClient();
            }
        });

        mLogo = (TextView) findViewById(R.id.logo);

        newRiderBtn = (Button) findViewById(R.id.new_rider);

        newRiderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateNewAnonRider();
            }
        });

        mDestFrag = (DestinationFragment) getSupportFragmentManager().findFragmentById(R.id.frag_dest_map);

        getSupportFragmentManager().beginTransaction().hide(mDestFrag).commitAllowingStateLoss();

        mDestinationBtn = (Button) findViewById(R.id.new_destination);

        mDestinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().show(mDestFrag).commitAllowingStateLoss();
                showingDestMap = true;
            }
        });

        mNewRiderInterface = (RelativeLayout) findViewById(R.id.new_rider_interface);

        loader = (AVLoadingIndicatorView) findViewById(R.id.avi);

        mMessageReceiver = new MessageReceiver();

        final TextView boyIcon = (TextView) findViewById(R.id.boy_icon);

        boyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            int count = 0;

            for (int i = 0; i < mPassengersadapter.getCount(); i++) {
                count += mPassengersadapter.getItem(i).count;
            }

            if (count >= 3) {
                    Toast.makeText(context, R.string.passengers_limit_reached, Toast.LENGTH_SHORT).show();
                    animateNewAnonRiderHide();
                    return;
                }

                try {

                    NewPassengerMessage newPassengerMessage = new NewPassengerMessage();

                    newPassengerMessage.gender = "M";

                    JSONObject jsonObject = new JSONObject();
                    newPassengerMessage.pack(jsonObject);
                    app.sendString(jsonObject.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        final TextView girlIcon = (TextView) findViewById(R.id.girl_icon);

        girlIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int count = 0;

                for (int i = 0; i < mPassengersadapter.getCount(); i++) {
                    count += mPassengersadapter.getItem(i).count;
                }

                if (count >= 3) {
                    Toast.makeText(context, R.string.passengers_limit_reached, Toast.LENGTH_SHORT).show();
                    animateNewAnonRiderHide();
                    return;
                }

                try {

                    NewPassengerMessage newPassengerMessage = new NewPassengerMessage();

                    newPassengerMessage.gender = "F";

                    JSONObject jsonObject = new JSONObject();
                    newPassengerMessage.pack(jsonObject);
                    app.sendString(jsonObject.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        passengerList = (ListView) findViewById(R.id.passengers);

        ArrayList<Passenger> savedPassengers = app.LoadSavedPassengers();

        ArrayList<Passenger> myArray = new ArrayList<>();

        if (savedPassengers != null) {
            for (Passenger p : savedPassengers) {
                myArray.add(p);
            }
        }

        if (app.isClientConnected())
            app.ReplaceHandler(this);

        DestinationFragment.DestInfo savedDestInfo = app.loadSavedDestInfo();

        if (savedDestInfo != null) {
            mDestFrag.setDestInfo(savedDestInfo);
        }

        mPassengersadapter = new PassengersAdapter(context, myArray, new PassengersAdapter.IDeletePassenger() {
            @Override
            public void OnDeletePassenger(Passenger p) {

                try {

                    if (p.type == Passenger.PassengerType.ANON) {
                        DeletePassengerMessage message = new DeletePassengerMessage();
                        message.passengerId = p.id;
                        JSONObject jsonWriter = new JSONObject();
                        message.pack(jsonWriter);
                        app.sendString(jsonWriter.toString());
                    }
                    else if (p.type == Passenger.PassengerType.CLIENT) {
                        DeleteRiderMessage message = new DeleteRiderMessage();
                        message.riderId = p.id;
                        JSONObject jsonWriter = new JSONObject();
                        message.pack(jsonWriter);
                        app.sendString(jsonWriter.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mDestFrag.refreshRiderList();

        _frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frame_layout);

        _frag.getMapAsync(this);

        getSupportFragmentManager().beginTransaction().hide(_frag).commit();

        passengerList.setAdapter(mPassengersadapter);

        passengerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Passenger p = (Passenger)passengerList.getItemAtPosition(position);

                if (p.type == Passenger.PassengerType.CLIENT) {
                    showingMap = true;

                    RiderInfoMessage riderInfo = app.getRiderInfo(p.id);

                    if (riderInfo == null)
                        return;

                    currentRiderIdOnMap = riderInfo.id;
                    LatLng riderPosition = new LatLng(riderInfo.locationLat, riderInfo.locationLong);

                    Mmarker.setPosition(riderPosition);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(riderPosition).zoom(15).build();

                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));

                    getSupportFragmentManager().beginTransaction().show(_frag).commit();
                }
            }
        });
        }catch (Exception e) {
            try {
                FirebaseCrash.report(e);
            }catch (Exception ee) {

            }
        }
    }

    @Override
    protected void onDestroy() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

        ArrayList<Passenger> passengers = new ArrayList<>();

        for (int i = 0; i < mPassengersadapter.getCount(); i++) {
            passengers.add(i, mPassengersadapter.getItem(i));
        }

        app.savePassengers(passengers);
        app.saveCurrentDestInfo(mDestFrag.getDestInfo());
        Log.e("destroyed", "destroyed main");

        super.onDestroy();
    }

    public void pushNotification(String msg, Bundle bundle, Class activity) {

        mNotificationId++;

        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, activity);
        intent.putExtras(bundle);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.drawable.white_logo)
                .setContentTitle(getString(R.string.new_client))
                .setColor(0xff448aff)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(
                        RingtoneManager
                                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public void animateNewAnonRider() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            animateNewAnonRiderPort();
        }
        else {
            animateNewAnonRiderLand();
        }
    }

    public void animateNewAnonRiderPort() {
        mNewRiderInterface.setVisibility(View.VISIBLE);

        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLogo.animate().alpha(0).translationY(70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLogo.setVisibility(View.GONE);
                mLogo.setAlpha(0);

            }
        });

        mNewRiderInterface.animate().alpha(1).translationY(70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mNewRiderInterface.setVisibility(View.VISIBLE);
                mNewRiderInterface.setAlpha(1);
            }
        });
    }

    public void animateNewAnonRiderLand() {

        mNewRiderInterface.setVisibility(View.VISIBLE);

        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mNewRiderInterface.animate().alpha(1).translationY(70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mNewRiderInterface.setVisibility(View.VISIBLE);
                mNewRiderInterface.setAlpha(1);
            }
        });
    }

    public void animateNewAnonRiderHide() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            animateNewAnonRiderHidePort();
        }
        else {
            animateNewAnonRiderHideLand();
        }
    }

    public void animateNewAnonRiderHidePort() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLogo.setVisibility(View.VISIBLE);

        mLogo.animate().alpha(1).translationY(-70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mLogo.setVisibility(View.VISIBLE);
                mLogo.setAlpha(1);
            }
        });

        mNewRiderInterface.animate().alpha(0).translationY(-70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mNewRiderInterface.setVisibility(View.GONE);
            }
        });
    }

    public void animateNewAnonRiderHideLand() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mNewRiderInterface.animate().alpha(0).translationY(-70).setDuration(shortAnimTime).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mNewRiderInterface.setVisibility(View.GONE);
            }
        });
    }

    public void requestLocationUpdates() {

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        else
            {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            if (!AppUtils.isLocationEnabled(context)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage(R.string.gps_disabled);
                dialog.setPositiveButton(R.string.location_params, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }


            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(LOCATION_REFRESH_TIME);
            mLocationRequest.setFastestInterval(LOCATION_REFRESH_TIME);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        currentLocation = null;
    }

    @Override
    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void connectWebSocket() {

       app.connectWebSocket(this);
    }

    public void animateShow() {
        try {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                animateShowPort();
            } else
                animateShowLand();
        }catch (Exception e) {
            try {
                FirebaseCrash.report(e);
            }catch (Exception ee) {

            }
            show();
        }
    }

    public void show() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            showPort();
        }
        else
            showLand();
    }

    public void showPort() {
        beginBtn.setAlpha(0);
        beginBtn.setVisibility(View.GONE);
        cancelBtn.setAlpha(1);
        cancelBtn.setVisibility(View.VISIBLE);
        newRiderBtn.setAlpha(1);
        newRiderBtn.setVisibility(View.VISIBLE);
        mDestinationBtn.setAlpha(1);
        mDestinationBtn.setVisibility(View.VISIBLE);
        loader.show();
    }

    public void animateShowPort() {

        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        beginBtn.setVisibility(View.GONE);
        beginBtn.animate().setDuration(shortAnimTime).alpha(
                0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                beginBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.VISIBLE);
                cancelBtn.animate().setDuration(shortAnimTime).alpha(
                        1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cancelBtn.setVisibility(View.VISIBLE);

                        newRiderBtn.setVisibility(View.VISIBLE);
                        newRiderBtn.animate().setDuration(shortAnimTime).alpha(
                                1).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                newRiderBtn.setVisibility(View.VISIBLE);

                                mDestinationBtn.setVisibility(View.VISIBLE);
                                mDestinationBtn.animate().setDuration(shortAnimTime).alpha(
                                        1).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mDestinationBtn.setVisibility(View.VISIBLE);

                                        loader.setVisibility(View.VISIBLE);
                                        loader.animate().setDuration(shortAnimTime).alpha(
                                                1).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                loader.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void showLand() {
        beginBtn.setAlpha(0);
        beginBtn.setVisibility(View.GONE);
        mLogo.setAlpha(0);
        mLogo.setVisibility(View.GONE);
        cancelBtn.setAlpha(1);
        cancelBtn.setVisibility(View.VISIBLE);
        newRiderBtn.setAlpha(1);
        newRiderBtn.setVisibility(View.VISIBLE);
        mDestinationBtn.setAlpha(1);
        mDestinationBtn.setVisibility(View.VISIBLE);
        loader.setAlpha(1);
        loader.show();
    }

    public void animateShowLand() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        beginBtn.setVisibility(View.GONE);
        beginBtn.animate().setDuration(shortAnimTime).alpha(
                0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                beginBtn.setVisibility(View.GONE);

                mLogo.setVisibility(View.GONE);
                mLogo.animate().setDuration(shortAnimTime).alpha(
                        0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLogo.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.VISIBLE);
                        cancelBtn.animate().setDuration(shortAnimTime).alpha(
                                1).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                cancelBtn.setVisibility(View.VISIBLE);

                                newRiderBtn.setVisibility(View.VISIBLE);
                                newRiderBtn.animate().setDuration(shortAnimTime).alpha(
                                        1).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        newRiderBtn.setVisibility(View.VISIBLE);

                                        mDestinationBtn.setVisibility(View.VISIBLE);
                                        mDestinationBtn.animate().setDuration(shortAnimTime).alpha(
                                                1).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                mDestinationBtn.setVisibility(View.VISIBLE);

                                                loader.setVisibility(View.VISIBLE);
                                                loader.animate().setDuration(shortAnimTime).alpha(
                                                        1).setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        loader.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void animateHide() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            animateHidePort();
        }
        else
            animateHideLand();
    }

    public void hidePort() {
        loader.setVisibility(View.INVISIBLE);
        mDestinationBtn.setVisibility(View.GONE);
        newRiderBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
        beginBtn.setVisibility(View.VISIBLE);
    }

    public void animateHidePort() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //loader.setVisibility(View.GONE);
        loader.animate().setDuration(shortAnimTime).alpha(
                0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loader.setVisibility(View.INVISIBLE);

                mDestinationBtn.animate().setDuration(shortAnimTime).alpha(
                        0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mDestinationBtn.setVisibility(View.GONE);

                        // newRiderBtn.setVisibility(View.GONE);
                        newRiderBtn.animate().setDuration(shortAnimTime).alpha(
                                0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                newRiderBtn.setVisibility(View.GONE);

                                // cancelBtn.setVisibility(View.GONE);
                                cancelBtn.animate().setDuration(shortAnimTime).alpha(
                                        0).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        cancelBtn.setVisibility(View.GONE);

                                        //   beginBtn.setVisibility(View.VISIBLE);
                                        beginBtn.animate().setDuration(shortAnimTime).alpha(
                                                1).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                beginBtn.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void hideLand() {
        loader.setVisibility(View.VISIBLE);
        mDestinationBtn.setVisibility(View.GONE);
        newRiderBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
        mLogo.setVisibility(View.VISIBLE);
        beginBtn.setVisibility(View.VISIBLE);
    }

    public void animateHideLand() {
        final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //loader.setVisibility(View.GONE);
        loader.animate().setDuration(shortAnimTime).alpha(
                0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loader.setVisibility(View.INVISIBLE);

                mDestinationBtn.animate().setDuration(shortAnimTime).alpha(
                        0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mDestinationBtn.setVisibility(View.GONE);

                        // newRiderBtn.setVisibility(View.GONE);
                        newRiderBtn.animate().setDuration(shortAnimTime).alpha(
                                0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                newRiderBtn.setVisibility(View.GONE);

                                // cancelBtn.setVisibility(View.GONE);
                                cancelBtn.animate().setDuration(shortAnimTime).alpha(
                                        0).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        cancelBtn.setVisibility(View.GONE);

                                        mLogo.animate().setDuration(shortAnimTime).alpha(
                                                1).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                mLogo.setVisibility(View.VISIBLE);

                                                //   beginBtn.setVisibility(View.VISIBLE);
                                                beginBtn.animate().setDuration(shortAnimTime).alpha(
                                                        1).setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        beginBtn.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();

        super.onStart();

        if (app.isClientConnected())
            animateShow();
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.e("Location", String.valueOf(location.getAccuracy()));

        if (mCurrentCity == null && location.getAccuracy() != 0) {
            findCurrentCity(location);
        }

        if (currentLocation == null && location.getAccuracy() < 100 && app.isClientConnected())
        {
            currentLocation = location;

            NewLocationMessage newLocationMessage = new NewLocationMessage();
            newLocationMessage.lat = location.getLatitude();
            newLocationMessage.lon = location.getLongitude();

            try {
                JSONObject jsonWriter = new JSONObject();
                newLocationMessage.pack(jsonWriter);
                app.sendString(jsonWriter.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return;
        }

        if ((location.getAccuracy() == 0 || location.getAccuracy() > 24 || !app.isClientConnected()))
            return;

        Log.e("first_location", String.valueOf(location.getAccuracy()));

        currentLocation = location;

        if (showingDestMap)
            mDestFrag.updateLocation();

        NewLocationMessage newLocationMessage = new NewLocationMessage();
        newLocationMessage.lat = location.getLatitude();
        newLocationMessage.lon = location.getLongitude();

        try {
            JSONObject jsonWriter = new JSONObject();
            newLocationMessage.pack(jsonWriter);
            app.sendString(jsonWriter.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void findCurrentCity(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> locations;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            locations = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);

            if (locations != null && locations.size() != 0 && locations.get(0).getLocality() != null && !locations.get(0).getLocality().equals("")) {
               mCurrentCity = locations.get(0).getLocality();
            }

        } catch (IOException | IllegalArgumentException ioException) {
            // Catch network or other I/O problems.
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Mmarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(-33, -7))
                .title("position"));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
    }

    public void UpdateRiderPosition(String id, double lat, double lon) {

        if (showingMap && currentRiderIdOnMap.equals(id)) {

            LatLng newRiderLoc = new LatLng(lat, lon);
            Mmarker.setPosition(newRiderLoc);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(newRiderLoc).zoom(mMap.getCameraPosition().zoom).build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    public void closeWaitingRiderMap(String riderId) {
        if (showingMap && currentRiderIdOnMap.equals(riderId)) {
            onBackPressed();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            // do things
        }

        if (app.isClientConnected())
            requestLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9002) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(context, data);

                mDestFrag.UpdateDestination(place.getLatLng());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

        AuthenticationMessage auth = new AuthenticationMessage();
        auth.email = mUser.getEmail();
        auth.token = mUser.getToken();
        auth.manufacturer = Build.MANUFACTURER;
        auth.model = Build.MODEL;

        try {
            JSONObject jsonWriter = new JSONObject();
            auth.pack(jsonWriter);
            app.sendConnectionString(jsonWriter.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        mMessageReceiver.Receive(context, message);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mPassengersadapter.clear();
                mPassengersadapter.notifyDataSetChanged();
                animateHide();
                animateNewAnonRiderHide();
                stopLocationUpdates();
                beginBtn.setEnabled(true);

                if (mDestFrag != null)
                    mDestFrag.clearMap();

                context.onBackPressed();
            }
        });
    }

    @Override
    public void onError(Exception e) {
        if (e != null && e.getMessage() != null && !e.getMessage().equals(""))
            Log.e("ERROR_HERE", e.getMessage());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!app.isClientConnected())
                    beginBtn.setEnabled(true);
                Toast.makeText(context, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pushDepartureArrivalNotif(String locAddress, String destAddress, Bundle b) {

        String str = getString(R.string.Departure)+": " + locAddress + "\n" +
                getString(R.string.Arrival)+": " + destAddress;

        pushNotification(str, b, RiderInfoActivity.class);
    }
}
