/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.socialappsharing.feeds;

import static android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.socialappsharing.broadcastreceivers.LocationBroadcastReceiver;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * This class refers the Load the Map for tag the location for feeds
 */
public class LoadMapForFeed extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_ACCESS_PERMISSION_CODE = 101;
    private static final int REQUEST_CODE_GPS = 102;
    private HuaweiMap hMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private SettingsClient settingsClient;
    private Context mContext;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private LocationManager locManager;
    private TextView mLocationDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_map);

        mContext = getApplicationContext();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        // Set the location update interval (int milliseconds).
        mLocationRequest.setInterval(10000);
        // Set the weight.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mMapView = findViewById(R.id.mapview_mapviewdemo);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        mLocationDetail = findViewById(R.id.loction_detail);

        TextView mDoneBtn = findViewById(R.id.done_btn);
        mDoneBtn.setVisibility(View.VISIBLE);
        mDoneBtn.setOnClickListener(view -> finish());

        ImageView mBackBtn = findViewById(R.id.back_btn);
        mBackBtn.setOnClickListener(view -> finish());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {

        hMap = huaweiMap;

        hMap.setMapType(HuaweiMap.MAP_TYPE_NORMAL);
        // Specify whether to enable the zoom controls.
        hMap.getUiSettings().setZoomControlsEnabled(true);
        // Set the preferred minimum zoom level.
        hMap.setMinZoomPreference(10f);
        // Set the preferred maximum zoom level.
        hMap.setMaxZoomPreference(18f);
        // Reset the maximum and minimum zoom levels.
        hMap.resetMinMaxZoomPreference();
        // Specify whether to enable the compass.
        hMap.getUiSettings().setCompassEnabled(false);
        // Specify whether to enable the zoom gestures.
        hMap.getUiSettings().setZoomGesturesEnabled(true);
        // Specify whether to enable the scroll gestures.
        hMap.getUiSettings().setScrollGesturesEnabled(true);
        // Specify whether to enable the tilt gestures.
        hMap.getUiSettings().setTiltGesturesEnabled(true);

        // add location button click listener
        hMap.setOnMyLocationButtonClickListener(() -> {
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getDeviceLocation();
            }
            return false;
        });

        hMap.setOnMapLongClickListener(this::moveCamera);

        if (checkPermission()) {
            getLastKnownLocation();
            checkLocationSettings();
            // Enable the my-location overlay.
            hMap.setMyLocationEnabled(true);
            // Enable the my-location icon.
            hMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            Toast.makeText(getApplicationContext(),"Permission needed",Toast.LENGTH_LONG).show();
            // Request the permissions
            requestPermission();
        }
    }

    // Request the permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_PERMISSION_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, REQUEST_ACCESS_PERMISSION_CODE);
        }
    }

    // Checks the permissions
    public boolean checkPermission() {
        int fineloc = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseloc = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            return fineloc == PackageManager.PERMISSION_GRANTED && coarseloc == PackageManager.PERMISSION_GRANTED;
        } else {
            int backloc = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            return fineloc == PackageManager.PERMISSION_GRANTED && coarseloc == PackageManager.PERMISSION_GRANTED && backloc == PackageManager.PERMISSION_GRANTED;
        }
    }


    // Request permissions results
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean fineloc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean coarseloc = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    if (fineloc
                            && coarseloc) {
                        getLastKnownLocation();
                        checkLocationSettings();
                        // Enable the my-location overlay.
                        hMap.setMyLocationEnabled(true);
                        // Enable the my-location icon.
                        hMap.getUiSettings().setMyLocationButtonEnabled(true);
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.access_needed), Toast.LENGTH_LONG).show();
                    }
                } else {
                    boolean backloc = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (fineloc
                            && coarseloc && backloc) {
                        getLastKnownLocation();
                        checkLocationSettings();
                        // Enable the my-location overlay.
                        hMap.setMyLocationEnabled(true);
                        // Enable the my-location icon.
                        hMap.getUiSettings().setMyLocationButtonEnabled(true);
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.access_needed), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GPS) {
            if (resultCode == Activity.RESULT_OK) {
                String provider = Settings.Secure.getString(mContext.getContentResolver(), LOCATION_PROVIDERS_ALLOWED);
                if (provider != null) {
                    getDeviceLocation();
                    checkLocationSettings();
                    // Enable the my-location overlay.
                    hMap.setMyLocationEnabled(true);
                    // Enable the my-location icon.
                    hMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            }
        }
    }

    private void getLastKnownLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        moveCamera(defaultLocation);
                        return;
                    }
                    // Processing logic of the Location object.
                    moveCamera(new LatLng(location.getLatitude(),location.getLongitude()));
                })
                .addOnFailureListener(e -> LogUtil.e("Res ", e.getMessage()));
    }

    // getting the devices current location
    private void getDeviceLocation() throws SecurityException {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        // Exception handling logic.
        Task<Location> task = fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
                    }else{
                        moveCamera(defaultLocation);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void moveCamera(LatLng latLng)  {
        // Set the center point and zoom level of the camera.
        float zoom= 15.0f;
        // Set the tilt.
        float tilt = 2.2f;
        // Set the bearing.
        float bearing = 31.5f;
        CameraPosition cameraPosition = new CameraPosition(latLng,zoom,tilt,bearing);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        // Move the map camera in animation mode.
        hMap.animateCamera(cameraUpdate);
        hMap.clear();
        hMap.addMarker(new MarkerOptions().position(latLng));

        try{
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            String mAddress = null;

            if (state != null & city != null && country !=null){
                mAddress = city +", "+state+", "+country;
            }else if (state != null && country !=null){
                mAddress = state+", "+country;
            }else if(country !=null){
                mAddress = country;
            }

            String mMsg = PreferenceHandler.getInstance(mContext).getFeedDescription();
            String finalAddress = mMsg +"  in "+mAddress;


            if (!PreferenceHandler.getInstance(mContext).getFeedTaggedFriends().isEmpty()) {
                StringBuilder sb1 = new StringBuilder();
                String mFeedTaggedFriends = PreferenceHandler.getInstance(mContext).getFeedTaggedFriends();
                finalAddress = sb1.append(mFeedTaggedFriends).toString();
                finalAddress = finalAddress +"  in "+mAddress;
            }

            PreferenceHandler.getInstance(mContext).setFeedDescription(finalAddress);
            PreferenceHandler.getInstance(mContext).setFeedAddress(mAddress);
            mLocationDetail.setText(mAddress);

        }catch (IOException e){
                LogUtil.e("Res error : " , e.getMessage());
        }
    }

    private void checkLocationSettings() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // Before requesting location update, invoke checkLocationSettings to check device settings.
            Task<LocationSettingsResponse> locationSettingsResponseTask = settingsClient.checkLocationSettings(locationSettingsRequest);
            locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, getPendingIntent())
                        .addOnSuccessListener(aVoid -> getPendingIntent())
                        .addOnFailureListener(Throwable::printStackTrace);
            });
            locationSettingsResponseTask.addOnFailureListener(e -> {
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        // When the startResolutionForResult is invoked, a dialog box is displayed, asking you to open the corresponding permission.
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(LoadMapForFeed.this, 0);
                    } catch (IntentSender.SendIntentException e1) {
                        LogUtil.e("Res error : ", e1.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.e(LoadMapForFeed.class.getSimpleName(),e.getMessage());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationBroadcastReceiver.class);
        intent.setAction(LocationBroadcastReceiver.ACTION_PROCESS_LOCATION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Alert Message No Gps
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoadMapForFeed.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS))
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsenabled = locManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsenabled) {
            buildAlertMessageNoGps();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
