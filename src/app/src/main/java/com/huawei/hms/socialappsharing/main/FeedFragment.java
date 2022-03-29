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

package com.huawei.hms.socialappsharing.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.feeds.FeedPreview;
import com.huawei.hms.socialappsharing.feeds.FeedsParentAdapter;
import com.huawei.hms.socialappsharing.feeds.LoadMapForFeedPost;
import com.huawei.hms.socialappsharing.models.Advertise;
import com.huawei.hms.socialappsharing.models.FeedModel;
import com.huawei.hms.socialappsharing.models.Feeds;
import com.huawei.hms.socialappsharing.models.Media;
import com.huawei.hms.socialappsharing.utils.CommonMember;
import com.huawei.hms.socialappsharing.utils.ObjectTypeInfoHelper;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.DBNAME;
import static com.huawei.hms.socialappsharing.utils.Constants.FEED_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIAID;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIATYPE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_COMMON_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_URI;

/**
 * This class loads the Feed list of the user
 */
@SuppressLint("StaticFieldLeak")
public class FeedFragment extends Fragment {
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZone mCloudDBZone;
    private static String mFeedID;
    private static String mFeedMediaId;
    private static String mFeedDescription;
    private static String mFeedLikes;
    private static String mFeedUnLikes;
    private static String mFeedUploadedBy;
    private static String mFeedUploadedDate;
    private static String mUserImageUploaded;
    private static final int CAMERA_REQUEST = 102;
    private static final int GALLERY_REQUEST = 103;
    private static final int REQUEST_VIDEO_CAPTURE = 104;
    private static final int MAP_REQUEST = 105;
    private Uri imageUri;
    public static int VIDEO_SECONDS = 30;
    private static Context mContext;
    private static Context mActivity;
    private static RecyclerView recyclerView;
    private static int mFeedTotalListFromServer;
    private static SwipeRefreshLayout swipeContainer;
    private static final ArrayList<String> FEED_ID_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_MEDIA_ID_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_DESCRIPTION_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_LIKES_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_UN_LIKES_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_UPLOADED_BY_LIST = new ArrayList<>();
    private static final ArrayList<String> FEED_UPLOADED_DATE_LIST = new ArrayList<>();
    private static final ArrayList<String> USER_IMAGE_UPLOADED_LIST = new ArrayList<>();
    private static final ArrayList<Object> FEED_MODEL_ARRAYLIST = new ArrayList<>();
    public static ArrayList<Uri> mImageUriList = new ArrayList<>();
    private static int mFeedCount = 0;
    public static ClipData mData;
    public static boolean mAdLoad = false;

    public static void refreshFeeds() {
        if (isNetworkConnected(mContext)) {
            mFeedCount = 0;
            showDialog((Activity) mActivity);
            cloudDBZoneCreation();
        } else {
            dismissDialog();
            networkStatusAlert(mContext);
        }
    }

    private static void onFailure(Exception e) {
        dismissDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        mContext = getContext();
        mActivity = getActivity();

        recyclerView = view.findViewById(R.id.feed_recyclerView);
        AppCompatImageView mCameraBtn = view.findViewById(R.id.camera_image);
        AppCompatImageView mVideoBtn = view.findViewById(R.id.video_image);
        AppCompatImageView mGalleryBtn = view.findViewById(R.id.gallery_image);
        AppCompatImageView mMapBtn = view.findViewById(R.id.map_image);


        mCameraBtn.setOnClickListener(v -> {
            if (checkPermissionForCamera()) {
                cameraIntent();
            } else {
                // Request the permissions
                requestPermissionForCamera();
            }
        });

        mVideoBtn.setOnClickListener(v -> {
            if (checkPermissionForVideo()) {
                videoIntent();
            } else {
                // Request the permissions
                requestPermissionForVideo();
            }
        });

        mGalleryBtn.setOnClickListener(v -> {
            if (checkPermissionForGallery()) {
                galleryIntent();
            } else {
                // Request the permissions
                requestPermissionForGallery();
            }
        });

        mMapBtn.setOnClickListener(v -> {
            if (checkPermissionForMap()) {
                Intent intent = new Intent(mContext, LoadMapForFeedPost.class);
                startActivity(intent);
            } else {
                // Request the permissions
                requestPermissionForMap();
            }
        });


        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            mFeedCount = 0;
            initiateTheCloud();
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);        return view;
    }

    private void initiateTheCloud() {
        if (isNetworkConnected(mContext)) {
            AGConnectCloudDB.initialize(mContext);
            mCloudDB = AGConnectCloudDB.getInstance();
            try {
                mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            } catch (AGConnectCloudDBException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
            cloudDBZoneCreation();
        } else {
            CommonMember.dismissDialog();
            networkStatusAlert(mContext);
        }
    }

    private static void cloudDBZoneCreation() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(DBNAME, CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE, CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            mCloudDBZone = cloudDBZone;
            queryAllFeeds();
        }).addOnFailureListener(FeedFragment::onFailure);
    }

    private static void queryAllFeeds() {
        mFeedCount = 0;
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Feeds>> queryTask = mCloudDBZone.executeQuery(CloudDBZoneQuery.where(Feeds.class).orderByDesc(FEED_ID), CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            FEED_MODEL_ARRAYLIST.clear();
            FEED_ID_LIST.clear();
            FEED_MEDIA_ID_LIST.clear();
            FEED_DESCRIPTION_LIST.clear();
            FEED_LIKES_LIST.clear();
            FEED_UN_LIKES_LIST.clear();
            FEED_UPLOADED_BY_LIST.clear();
            FEED_UPLOADED_DATE_LIST.clear();
            USER_IMAGE_UPLOADED_LIST.clear();

            mFeedTotalListFromServer = snapshot.getSnapshotObjects().size();
            for (int i = 0; i < mFeedTotalListFromServer; i++) {
                try {
                    mFeedID = snapshot.getSnapshotObjects().get(i).getFeedId();
                    mFeedMediaId = snapshot.getSnapshotObjects().get(i).getMediaId();
                    mFeedDescription = snapshot.getSnapshotObjects().get(i).getFeedDescription();
                    mFeedLikes = snapshot.getSnapshotObjects().get(i).getFeedLikes();
                    mFeedUnLikes = snapshot.getSnapshotObjects().get(i).getFeedUnlikes();
                    mFeedUploadedBy = snapshot.getSnapshotObjects().get(i).getUploadedBy();
                    mFeedUploadedDate = snapshot.getSnapshotObjects().get(i).getUploadedDate();
                    mUserImageUploaded = snapshot.getSnapshotObjects().get(i).getUserImage();

                    FEED_ID_LIST.add(mFeedID);
                    FEED_MEDIA_ID_LIST.add(mFeedMediaId);
                    FEED_DESCRIPTION_LIST.add(mFeedDescription);
                    FEED_LIKES_LIST.add(mFeedLikes);
                    FEED_UN_LIKES_LIST.add(mFeedUnLikes);
                    FEED_UPLOADED_BY_LIST.add(mFeedUploadedBy);
                    FEED_UPLOADED_DATE_LIST.add(mFeedUploadedDate);
                    USER_IMAGE_UPLOADED_LIST.add(mUserImageUploaded);

                    CloudDBZoneQuery<Media> mMediaQuery = CloudDBZoneQuery.where(Media.class).equalTo(MEDIA_COMMON_ID, mFeedMediaId);
                    querymedia(mMediaQuery);

                } catch (AGConnectCloudDBException e) {
                    dismissDialog();
                    LogUtil.e("Res error : ", e.getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            dismissDialog();
            queryAllFeeds();
        });
    }

    private static void processMediaQueryResult(CloudDBZoneSnapshot<Media> snapshot) {
        CloudDBZoneObjectList<Media> mediaInfoCursor = snapshot.getSnapshotObjects();
        try {
            ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            while (mediaInfoCursor.hasNext()) {
                HashMap<String, String> hashmap = new HashMap<>();
                Media mMedia = mediaInfoCursor.next();

                hashmap.put(MEDIA_COMMON_ID, mMedia.getMediaCommonId());
                hashmap.put(MEDIATYPE, mMedia.getMediaType());
                hashmap.put(MEDIAID, mMedia.getMediaId());
                hashmap.put(MEDIA_URI, mMedia.getMediaURI());

                arrayList.add(hashmap);
            }
            try {
                mFeedID = FEED_ID_LIST.get(mFeedCount);
                mFeedMediaId = FEED_MEDIA_ID_LIST.get(mFeedCount);
                mFeedDescription = FEED_DESCRIPTION_LIST.get(mFeedCount);
                mFeedLikes = FEED_LIKES_LIST.get(mFeedCount);
                mFeedUnLikes = FEED_UN_LIKES_LIST.get(mFeedCount);
                mFeedUploadedBy = FEED_UPLOADED_BY_LIST.get(mFeedCount);
                mFeedUploadedDate = FEED_UPLOADED_DATE_LIST.get(mFeedCount);
                mUserImageUploaded = USER_IMAGE_UPLOADED_LIST.get(mFeedCount);

                FEED_MODEL_ARRAYLIST.add(new FeedModel(mFeedID, mFeedMediaId, mFeedDescription, mFeedLikes, mFeedUnLikes, mFeedUploadedBy, mFeedUploadedDate, mUserImageUploaded, arrayList));
                mFeedCount++;
                FEED_MODEL_ARRAYLIST.add(new Advertise("testy63txaom86"));

            } catch (IndexOutOfBoundsException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
            if (mFeedCount + 1 == mFeedTotalListFromServer) {
                swipeContainer.setRefreshing(false);

                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
                FeedsParentAdapter parentItemAdapter = new FeedsParentAdapter(mContext, FEED_MODEL_ARRAYLIST, mCloudDBZone, (FragmentActivity) mActivity);

                recyclerView.setAdapter(parentItemAdapter);
                recyclerView.setLayoutManager(layoutManager);
                dismissDialog();
            }
        } catch (AGConnectCloudDBException | IndexOutOfBoundsException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }


    private static void querymedia(CloudDBZoneQuery<Media> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Media>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(FeedFragment::processMediaQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForCamera() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForVideo() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_VIDEO_CAPTURE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForGallery() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQUEST);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionForMap() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MAP_REQUEST);
    }


    // Checks the permissions
    private boolean checkPermissionForCamera() {
        int camera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        int readexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return camera == PackageManager.PERMISSION_GRANTED && readexternal == PackageManager.PERMISSION_GRANTED && writeexternal == PackageManager.PERMISSION_GRANTED;
    }

    // Checks the permissions
    private boolean checkPermissionForVideo() {
        int camera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        int audio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
        int readexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return camera == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED && readexternal == PackageManager.PERMISSION_GRANTED && writeexternal == PackageManager.PERMISSION_GRANTED;
    }

    // Checks the permissions
    private boolean checkPermissionForGallery() {
        int readexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readexternal == PackageManager.PERMISSION_GRANTED && writeexternal == PackageManager.PERMISSION_GRANTED;
    }

    // Checks the permissions
    private boolean checkPermissionForMap() {
        int readexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeexternal = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readexternal == PackageManager.PERMISSION_GRANTED && writeexternal == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    showDialogForFeeds(data, 1);
                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    showDialogForFeeds(data, 2);
                }
                break;
            case GALLERY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    showDialogForFeeds(data, 3);
                }
                break;

            case MAP_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(mContext, LoadMapForFeedPost.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void showDialogForFeeds(Intent data, int count) {
        mAdLoad = false;
        if (count == 3) {
            if (data.getClipData() != null) {
                mData = data.getClipData();
                for (int i = 0; i < mData.getItemCount(); i++) {
                    ClipData.Item mItem = mData.getItemAt(i);
                    mImageUriList.add(mItem.getUri());
                }
                Intent intent = new Intent(mContext, FeedPreview.class);
                startActivity(intent);

            } else if (data.getData() != null) {
                mImageUriList.add(data.getData());
                Intent intent = new Intent(mContext, FeedPreview.class);
                startActivity(intent);
            } else {
                CommonMember.getErrorDialog(getResources().getString(R.string.media_not_valid), mContext).show();
            }
        } else if (count == 2) {
            if (data.getData() != null) {
                mImageUriList.add(data.getData());
                Intent intent = new Intent(mContext, FeedPreview.class);
                startActivity(intent);
            } else {
                CommonMember.getErrorDialog(getResources().getString(R.string.media_not_valid), mContext).show();
            }
        } else {
            mImageUriList.add(imageUri);
            Intent intent = new Intent(mContext, FeedPreview.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0) {
                    boolean camera =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    boolean readexternal =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    boolean writeexternal =
                            grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (camera && readexternal && writeexternal) {
                        Toast.makeText(mContext, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                        cameraIntent();
                    } else {
                        Toast.makeText(mContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                if (grantResults.length > 0) {
                    boolean camera =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean audio =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readexternal =
                            grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean writeexternal =
                            grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (camera && audio && readexternal && writeexternal) {
                        Toast.makeText(mContext, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                        videoIntent();
                    } else {
                        Toast.makeText(mContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case GALLERY_REQUEST:
                if (grantResults.length > 0) {

                    boolean readexternal =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    boolean writeexternal =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (readexternal && writeexternal) {
                        Toast.makeText(mContext, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                        galleryIntent();
                    } else {
                        Toast.makeText(mContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case MAP_REQUEST:
                if (grantResults.length > 0) {

                    boolean readexternal =
                            grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    boolean writeexternal =
                            grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (readexternal && writeexternal) {
                        Toast.makeText(mContext, getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(mContext, LoadMapForFeedPost.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }


    // Initiate the camera
    @SuppressLint("QueryPermissionsNeeded")
    private void cameraIntent() {
        int photonum = PreferenceHandler.getInstance(mContext).getImageName();
        PreferenceHandler.getInstance(mContext).setImageName(++photonum);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, photonum);
        imageUri = mContext.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (picIntent.resolveActivity(mContext.getPackageManager()) != null) {
            picIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(picIntent, CAMERA_REQUEST);
        }
    }

    // Initiate the video
    @SuppressLint("QueryPermissionsNeeded")
    private void videoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(mContext.getPackageManager()) != null) {
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEO_SECONDS);
            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20971520L);
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    // Initiate the gallery
    private void galleryIntent() {
        Intent galleryPickerIntent = new Intent(Intent.ACTION_PICK);
        // allowing multiple image to be selected
        galleryPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryPickerIntent.setType("*/*");
        galleryPickerIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20971520L);
        galleryPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        startActivityForResult(galleryPickerIntent, GALLERY_REQUEST);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFeedCount = 0;
        initiateTheCloud();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}