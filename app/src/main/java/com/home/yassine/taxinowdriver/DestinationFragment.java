package com.home.yassine.taxinowdriver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.ui.IconGenerator;
import com.home.yassine.taxinowdriver.protocol.NewDestinationMessage;
import com.home.yassine.taxinowdriver.protocol.RemoveDestinationMessage;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import com.home.yassine.taxinowdriver.types.LatLon;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yassine on 24/09/2016.
 */
public class DestinationFragment extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment _frag;
    private GoogleMap mDestinationMap;
    private Marker mMarker;
    private LatLng mCenterLatLong;
    public MainActivity hostActivity;
    private Bitmap mPositionIcon;
    public LatLng mDestinationLatLon;
    public Polyline mDrawnPolyline;
    private TextView riders[];
    private ShapeDrawable RoundDrawable;
    private DestInfo destInfo;

    public DestinationFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_destination_map, container, false);

        _frag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        _frag.getMapAsync(this);

        AppCompatButton searchPlace = (AppCompatButton) v.findViewById(R.id.place_btn);

        searchPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });

        AppCompatButton setDestinationBtn = (AppCompatButton) v.findViewById(R.id.set_dest);

        setDestinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextDestination(mCenterLatLong);
            }
        });

        AppCompatButton cancel = (AppCompatButton) v.findViewById(R.id.cancel_map_dest);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostActivity.onBackPressed();
            }
        });

        ImageView myPositionImage = (ImageView) v.findViewById(R.id.my_position);

        myPositionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mDestinationMap == null || hostActivity.currentLocation == null)
                    return;

                LatLng currentPosition = new LatLng(
                        hostActivity.currentLocation.getLatitude(),
                        hostActivity.currentLocation.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentPosition).zoom(mDestinationMap.getCameraPosition().zoom).build();

                mDestinationMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }
        });

        ImageView clearDestImage = (ImageView) v.findViewById(R.id.clear_dest);

        clearDestImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mDestinationLatLon == null)
                    return;

                mDrawnPolyline.remove();
                mDestinationLatLon = null;

                RemoveDestinationMessage removeDestinationMessage = new RemoveDestinationMessage();

                try {
                    JSONObject jsonWriter = new JSONObject();

                    removeDestinationMessage.pack(jsonWriter);
                    hostActivity.app.sendString(jsonWriter.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        riders = new TextView[3];

        riders[0] = (TextView) v.findViewById(R.id.rider1);
        riders[1] = (TextView) v.findViewById(R.id.rider2);
        riders[2] = (TextView) v.findViewById(R.id.rider3);

        RoundDrawable = new ShapeDrawable(new OvalShape());
        RoundDrawable.getPaint().setColor(0xfff);


        riders[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < hostActivity.mPassengersadapter.getCount(); i++) {

                    Passenger p = hostActivity.mPassengersadapter.getItem(i);

                    if (p.type != Passenger.PassengerType.CLIENT) {
                        continue;
                    }

                    RiderInfoMessage riderInfo = hostActivity.app.getRiderInfo(p.id);

                    setNextDestination(new LatLng(riderInfo.destinationLat, riderInfo.destinationLong));

                    break;
                }
            }
        });

        riders[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int j = 0;

                for (int i = 0; i < hostActivity.mPassengersadapter.getCount(); i++) {

                    Passenger p = hostActivity.mPassengersadapter.getItem(i);

                    if (p.type != Passenger.PassengerType.CLIENT) {
                        continue;
                    }

                    if (j == 1) {

                        RiderInfoMessage riderInfo = hostActivity.app.getRiderInfo(p.id);

                        setNextDestination(new LatLng(riderInfo.destinationLat, riderInfo.destinationLong));

                        break;
                    }

                    j++;
                }
            }
        });

        riders[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int j = 0;

                for (int i = 0; i < hostActivity.mPassengersadapter.getCount(); i++) {

                    Passenger p = hostActivity.mPassengersadapter.getItem(i);

                    if (p.type != Passenger.PassengerType.CLIENT) {
                        continue;
                    }

                    if (j == 2) {

                        RiderInfoMessage riderInfo = hostActivity.app.getRiderInfo(p.id);

                        setNextDestination(new LatLng(riderInfo.destinationLat, riderInfo.destinationLong));

                        break;
                    }

                    j++;
                }
            }
        });

        return v;
    }

    private void setNextDestination(LatLng destination) {

        try {

            mDestinationLatLon = destination;

            float results[] = new float[10];

            Location.distanceBetween(
                    hostActivity.currentLocation.getLatitude(),
                    hostActivity.currentLocation.getLongitude(),
                    mDestinationLatLon.latitude,
                    mDestinationLatLon.longitude,
                    results);

            float zoom;

            if (results[0] < 100)
                zoom = 17;
            else if (results[0] < 500)
                zoom = 16;
            else if (results[0] < 1000)
                zoom = 15;
            else if (results[0] < 4000)
                zoom = 14;
            else if (results[0] < 7000)
                zoom = 13;
            else
                zoom = 12;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mDestinationLatLon).zoom(zoom).build();

            mDestinationMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            GoogleDirection.withServerKey("AIzaSyB6vIqW0tUqHYXmQy-B7f0TQuKYl3tLmic")
                    .from(new LatLng(hostActivity.currentLocation.getLatitude(), hostActivity.currentLocation.getLongitude()))
                    .to(mDestinationLatLon)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            try {

                                ArrayList<LatLng> points = direction.getRouteList().get(0).getLegList()
                                        .get(0).getDirectionPoint();

                                NewDestinationMessage newDestinationMessage = new NewDestinationMessage();
                                newDestinationMessage.path = new ArrayList<>();

                                for (LatLng latLng : points) {

                                    LatLon latLon = new LatLon();
                                    latLon.lat = latLng.latitude;
                                    latLon.lon = latLng.longitude;

                                    newDestinationMessage.path.add(latLon);
                                }

                                DrawPolyline(points);

                                JSONObject jsonWriter = new JSONObject();

                                newDestinationMessage.pack(jsonWriter);
                                hostActivity.app.sendString(jsonWriter.toString());

                            } catch (Exception e) {
                                Log.e("err", e.getMessage());
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            try {
                                Log.e("ERRR", t.getMessage());
                            } catch (Exception e) {
                                Log.e("err", e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e("err", e.getMessage());
        }
    }

    public void DrawPolyline(ArrayList<LatLng> points) {

        PolylineOptions polylineOptions = DirectionConverter.createPolyline(
                hostActivity, points, 5,
                hostActivity.getResources() == null ?
                        Color.BLUE :
                        hostActivity.getResources().getColor(R.color.colorPrimary));

        if (mDrawnPolyline != null)
            mDrawnPolyline.remove();

        mDrawnPolyline = mDestinationMap.addPolyline(polylineOptions);
    }

    @Override
    public void onStart() {
        super.onStart();

        IconGenerator iconGenerator = new IconGenerator(hostActivity);
        mPositionIcon = iconGenerator.makeIcon(hostActivity.getString(R.string.actual_position));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("ATTACH", "attached");
        hostActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("DETACH", "detached");
        hostActivity = null;

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {

            if (mDestinationMap == null)
                return;

            mDestinationMap.getUiSettings().setMyLocationButtonEnabled(false);

            if (hostActivity.currentLocation == null)
                return;

            LatLng currentPosition = new LatLng(
                    hostActivity.currentLocation.getLatitude(),
                    hostActivity.currentLocation.getLongitude());

            if (mMarker != null)
                mMarker.remove();

            mMarker = mDestinationMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(mPositionIcon))
                    .position(currentPosition)
                    .title("position").alpha(.9f));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mDestinationLatLon != null ? mDestinationLatLon : currentPosition)
                    .zoom(mDestinationLatLon != null ? mDestinationMap.getCameraPosition().zoom : 15).build();

            mDestinationMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            mDestinationMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mDestinationMap = googleMap;

        getView().findViewById(R.id.locationMarker).setVisibility(View.VISIBLE);

        mDestinationMap.getUiSettings().setMyLocationButtonEnabled(true);
        mDestinationMap.setMyLocationEnabled(false);

        mDestinationMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mCenterLatLong = cameraPosition.target;
            }
        });

        if (destInfo != null) {
            mDestinationLatLon = destInfo.destLatLng;
            UpdateDestination(mDestinationLatLon);
            DrawPolyline(new ArrayList<>(destInfo.polyline.getPoints()));
        }
    }

    public void updateLocation() {

        if (mDestinationMap == null || hostActivity == null)
            return;

        LatLng currentPosition = new LatLng(
                hostActivity.currentLocation.getLatitude(),
                hostActivity.currentLocation.getLongitude());

        if (mMarker != null)
            mMarker.remove();

        mMarker = mDestinationMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(mPositionIcon))
                .position(currentPosition)
                .title("position").alpha(.9f));

        // we didn't get location at opening of map
        if(!mDestinationMap.getUiSettings().isMyLocationButtonEnabled()) {

            mDestinationMap.getUiSettings().setMyLocationButtonEnabled(true);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentPosition)
                    .zoom(15).build();

            mDestinationMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .zzku(hostActivity.mCurrentCity != null && !hostActivity.mCurrentCity.equals("") ? hostActivity.mCurrentCity.concat(", ") : "")
                    .build(hostActivity);
            hostActivity.startActivityForResult(intent, 9002);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(hostActivity, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = hostActivity.getString(R.string.playsercices_not_enabled) +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(hostActivity, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void UpdateDestination(LatLng latLng) {

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(14f).build();

        mDestinationMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    public void refreshRiderList() {

        for(TextView rider : riders) {
            rider.setTag(0);
        }

        for (int i = 0; i < hostActivity.mPassengersadapter.getCount(); i++) {

            Passenger currentPassenger = hostActivity.mPassengersadapter.getItem(i);

            if (currentPassenger.type != Passenger.PassengerType.CLIENT)
                continue;

            for(TextView rider : riders) {

                if (rider.getTag() != 0)
                    continue;

                int resourceId = hostActivity.getResources()
                        .getIdentifier(currentPassenger.iconName, "drawable", hostActivity.getPackageName());


                Drawable da[] = new Drawable[2];

                da[0] = RoundDrawable;
                da[1] = hostActivity.mPassengersadapter.getDrawable(resourceId);

                LayerDrawable layerDrawable = new LayerDrawable(da);

                rider.setBackground(layerDrawable);
                rider.setTag(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rider.setElevation(15);
                }
                rider.setVisibility(View.VISIBLE);
                break;
            }
        }

        for(TextView rider : riders) {
            if (rider.getTag() == 0)
                rider.setVisibility(View.GONE);
        }
    }

    public DestInfo getDestInfo() {

        if (mDestinationLatLon == null)
            return null;

        DestInfo destInfo = new DestInfo();
        destInfo.destLatLng = mDestinationLatLon;
        destInfo.polyline = mDrawnPolyline;

        return destInfo;
    }

    public void setDestInfo(DestInfo destInfo) {
        this.destInfo = destInfo;
    }

    public void drawPath(ArrayList<LatLon> path) {

        for (LatLon point : path) {

            mDestinationMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.lat, point.lon))
                    .title("position"));
        }
    }

    public void clearMap() {

        if (mDestinationMap != null) {
            mDestinationMap.clear();
        }
    }

    public class DestInfo {
        public LatLng destLatLng;
        public Polyline polyline;
    }
}
