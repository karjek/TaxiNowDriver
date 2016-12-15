package com.home.yassine.taxinowdriver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.concurrent.Exchanger;


public class ShowPathFragment extends Fragment implements OnMapReadyCallback {

    private RiderInfoActivity hostActivity;
    private double locLat;
    private double locLong;
    private double destLat;
    private double destLong;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private ShowPathFragment _frag = this;

    public ShowPathFragment() {
        // Required empty public constructor
    }

    public static ShowPathFragment newInstance(double locLat, double locLong,
                                               double destLat, double destLong) {
        ShowPathFragment fragment = new ShowPathFragment();
        Bundle args = new Bundle();
        args.putDouble("locLat", locLat);
        args.putDouble("locLong", locLong);
        args.putDouble("destLat", destLat);
        args.putDouble("destLong", destLong);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            locLat = getArguments().getDouble("locLat");
            locLong = getArguments().getDouble("locLong");
            destLat = getArguments().getDouble("destLat");
            destLong = getArguments().getDouble("destLong");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_show_path, container, false);

        mMapFragment = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                mMapFragment.getMapAsync(_frag);
            }
        };

        getChildFragmentManager().beginTransaction().add(R.id.showPath_frame, mMapFragment).commit();

        return v;
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {

            IconGenerator iconGenerator = new IconGenerator(hostActivity);

            Bitmap icon = iconGenerator.makeIcon(hostActivity.getString(R.string.Departure));
            Bitmap destIcon = iconGenerator.makeIcon(hostActivity.getString(R.string.Arrival));

            LatLng location = new LatLng(locLat, locLong);
            LatLng destination = new LatLng(destLat, destLong);

            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(location)
                    .title(hostActivity.getString(R.string.Departure)));

            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(destIcon))
                    .position(destination)
                    .title(hostActivity.getString(R.string.Arrival)));

            float results[] = new float[10];
            Location.distanceBetween(locLat, locLong, destLat, destLong, results);

            float zoom;

            if (results[0] < 100)
                zoom = 17;
            else if (results[0] < 500)
                zoom = 16;
            else if (results[0] < 1000)
                zoom = 15;
            else if (results[0] < 4000)
                zoom = 14;
            else
                zoom = 13;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location).zoom(zoom).build();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            GoogleDirection.withServerKey("AIzaSyB6vIqW0tUqHYXmQy-B7f0TQuKYl3tLmic")
                    .from(location)
                    .to(new LatLng(destLat, destLong))
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            try {
                                ArrayList<LatLng> points = direction.getRouteList().get(0).getLegList()
                                        .get(0).getDirectionPoint();

                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(
                                        hostActivity, points, 5,
                                        hostActivity.getResources() == null ?
                                                Color.BLUE :
                                                hostActivity.getResources().getColor(R.color.colorPrimary));

                                mMap.addPolyline(polylineOptions);
                            }catch (Exception e) {
                                Log.e("err", e.getMessage());
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            try {
                                Log.e("ERRR", t.getMessage());
                            }catch (Exception e) {
                                Log.e("err", e.getMessage());
                            }
                        }
                    });
        }catch (Exception e)
        {
            Log.e("err", e.getMessage());
        }
    }
}
