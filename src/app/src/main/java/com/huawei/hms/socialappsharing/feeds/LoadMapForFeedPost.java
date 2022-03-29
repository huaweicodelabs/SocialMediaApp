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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.broadcastreceivers.LocationBroadcastReceiver;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED;
import static com.huawei.hms.socialappsharing.main.FeedFragment.mAdLoad;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getTimeStamp;

/**
 * This class refers the Load the Map for tag the location for feeds
 */
public class LoadMapForFeedPost extends AppCompatActivity implements OnMapReadyCallback {
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
    public static Uri imageUri;
    private String mUserName;
    private boolean mUserNameAlreadyAdded = false;
    private LinearLayout mInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_map);

        mContext = getApplicationContext();
        mUserName = PreferenceHandler.getInstance(mContext).getDisplayName();

        PreferenceHandler.getInstance(mContext).setFeedAddress("");
        PreferenceHandler.getInstance(mContext).setFeedDescription("");
        PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
        PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
        PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");

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

        mInfoLayout = findViewById(R.id.info_layout);
        TextView mDoneBtn = findViewById(R.id.done_btn);
        mDoneBtn.setVisibility(View.VISIBLE);


        mDoneBtn.setOnClickListener(view -> hMap.snapshot(bitmap -> {
            if(bitmap !=null){
                mInfoLayout.setVisibility(View.GONE);
                String mRandomNumber = getTimeStamp();
                bitmapToFile(bitmap,mRandomNumber+".png");
            }
        }));


        ImageView mBackBtn = findViewById(R.id.back_btn);
        mBackBtn.setOnClickListener(view -> {
            PreferenceHandler.getInstance(mContext).setFeedAddress("");
            PreferenceHandler.getInstance(mContext).setFeedDescription("");
            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
            PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
            PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");
        });
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

        hMap.setOnMapLongClickListener(latLng -> {
            PreferenceHandler.getInstance(mContext).setFeedDescription("");
            PreferenceHandler.getInstance(mContext).setFeedAddress("");
            PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
            PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");
            moveCamera(latLng);
        });

        if (checkPermission()) {
            getLastKnownLocation();
            checkLocationSettings();
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
                }).addOnFailureListener(e -> LogUtil.e("Res ", e.getMessage()));
    }

    // getting the devices current location
    private void getDeviceLocation() throws SecurityException {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        // Exception handling logic.
        fusedLocationProviderClient.getLastLocation()
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
        float zoom= 8.0f;
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
            String finalAddress = null;

            if(mMsg.isEmpty()){
                finalAddress = mAddress;
            }

            if(mMsg.contains(PreferenceHandler.getInstance(mContext).getDisplayName())){
                mUserNameAlreadyAdded = true;
            }
            if (!PreferenceHandler.getInstance(mContext).getFeedTaggedFriends().isEmpty()) {
                StringBuilder sb1 = new StringBuilder();
                String mFeedTaggedFriends = PreferenceHandler.getInstance(mContext).getFeedTaggedFriends();
                finalAddress = sb1.append(mFeedTaggedFriends).toString();

                if(!mUserNameAlreadyAdded){
                    finalAddress = mUserName+" with" +finalAddress +"  in "+mAddress+ "  "+mMsg;
                }else{
                    finalAddress = finalAddress +"  in "+mAddress+ "  "+mMsg;
                }
            }else{
                if(!mUserNameAlreadyAdded){
                    finalAddress = mUserName+"  in "+mAddress+ "  "+mMsg;
                }
            }
            PreferenceHandler.getInstance(mContext).setFeedDescription(finalAddress);
            PreferenceHandler.getInstance(mContext).setFeedAddress(mAddress);
            PreferenceHandler.getInstance(mContext).setFeedMapLatitude(String.valueOf(latLng.latitude));
            PreferenceHandler.getInstance(mContext).setFeedMapLongtitude(String.valueOf(latLng.longitude));
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
            locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, getPendingIntent())
                    .addOnSuccessListener(aVoid -> getPendingIntent())
                    .addOnFailureListener(Throwable::printStackTrace)).addOnFailureListener(e -> {
                        int statusCode = ((ApiException) e).getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                // When the startResolutionForResult is invoked, a dialog box is displayed, asking you to open the corresponding permission.
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(LoadMapForFeedPost.this, 0);
                            } catch (IntentSender.SendIntentException e1) {
                                LogUtil.e("Res error : " , e1.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            LogUtil.e(LoadMapForFeedPost.class.getSimpleName(),e.getMessage());
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationBroadcastReceiver.class);
        intent.setAction(LocationBroadcastReceiver.ACTION_PROCESS_LOCATION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Remove Location Update
     */
    private void removeLocatonUpdatesWithIntent() {
        try {
            Task<Void> voidTask = fusedLocationProviderClient.removeLocationUpdates(getPendingIntent());
            voidTask.addOnFailureListener(Throwable::printStackTrace);
        } catch (Exception e) {
            LogUtil.e(LoadMapForFeedPost.class.getSimpleName(),e.getMessage());
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoadMapForFeedPost.this);
        builder.setMessage(getString(R.string.enable_gps))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS))
                .setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void bitmapToFile(Bitmap bitmap, String filename) { // File name like "image.png"
        // create a file to write bitmap data
        File file;
        try {
            file = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
            try{
                if (file.exists()){
                    file.delete();
                }
                file.createNewFile();
            }catch (SecurityException|IOException e) {
                LogUtil.e("Res error : " , e.getMessage());
            }

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();

            // write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            imageUri = Uri.fromFile(file);
            Intent intent = new Intent(mContext, FeedPreviewForMap.class);
            startActivity(intent);
        } catch (Exception e) {
            mInfoLayout.setVisibility(View.VISIBLE);
                LogUtil.e("Res error : " , e.getMessage());
            Toast.makeText(mContext, getString(R.string.unable_to_process_your_request), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdLoad){
            mAdLoad = false;
            finish();
        }

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
        removeLocatonUpdatesWithIntent();
        mUserNameAlreadyAdded = false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PreferenceHandler.getInstance(mContext).setFeedAddress("");
        PreferenceHandler.getInstance(mContext).setFeedDescription("");
        PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
        PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
        PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");
    }
}
