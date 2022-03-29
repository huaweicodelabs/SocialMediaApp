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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.agconnect.AGCRoutePolicy;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.cloud.storage.core.UploadTask;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.socialappsharing.ads.SplashAd;
import com.huawei.hms.socialappsharing.pushmessage.PushApis;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.models.Feeds;
import com.huawei.hms.socialappsharing.models.Media;
import com.huawei.hms.socialappsharing.models.NotificationList;
import com.huawei.hms.socialappsharing.models.Users;
import com.huawei.hms.socialappsharing.utils.CommonMember;
import com.huawei.hms.socialappsharing.utils.ObjectTypeInfoHelper;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.huawei.hms.socialappsharing.main.FeedFragment.mAdLoad;
import static com.huawei.hms.socialappsharing.main.FeedFragment.mImageUriList;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getPath;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getTimeStamp;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.Constants.DBNAME;
import static com.huawei.hms.socialappsharing.utils.Constants.FEED_TO_ALL;
import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_TYPE;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FEED;
import static com.huawei.hms.socialappsharing.utils.Constants.POSTED_FEED;
import static com.huawei.hms.socialappsharing.utils.Constants.STORAGE_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.VIDEO;

/**
 * This class refers the Feed Preview of the selected files
 */
public class FeedPreview extends AppCompatActivity {

    private Context mContext;
    private final ArrayList<String> horizontalList = new ArrayList<>();
    private final ArrayList<String> horizontalListFeedType = new ArrayList<>();
    private AppCompatImageView imagePreview;
    private AppCompatEditText descriptionPost;
    private AGCStorageManagement storageManagement;
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZone mCloudDBZone;
    private int mUserTotalListFromServer;
    private ArrayList<String> mUserArraylist;
    private ArrayList<String> mUserTokenArraylist;
    private final ArrayList<String> mSelectedUserList = new ArrayList<>();
    private static final int CAMERA_REQUEST = 105;
    private static final int GALLERY_REQUEST = 106;
    private static final int REQUEST_VIDEO_CAPTURE = 107;
    private String mMediaCommonId;
    public static ClipData mData;
    private CustomAdapter horizontalAdapter;
    private String mFeedType;
    private String mUserName;
    private String mUserImage;
    private static int mPostMediaCount = 0;
    private RecyclerView horizontal_recycler_view;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_layout);

        mContext = getApplicationContext();
        mUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
        mUserImage = PreferenceHandler.getInstance(mContext).getPhotoURL();

        mAdLoad = false;
        initiateTheCloud();
        mPostMediaCount = 0;

        ImageView backBtn = findViewById(R.id.back_btn);
        horizontal_recycler_view = findViewById(R.id.preview_recycler_imageview);
        imagePreview = findViewById(R.id.image_preview);
        AppCompatButton addImage = findViewById(R.id.add_image);
        AppCompatButton tagImage = findViewById(R.id.tag_image);
        AppCompatButton locationImage = findViewById(R.id.location_image);
        AppCompatButton postimage = findViewById(R.id.post_image);
        descriptionPost = findViewById(R.id.description_post);

        horizontalAdapter = new CustomAdapter(mContext, horizontalList, horizontalListFeedType);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(FeedPreview.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        horizontal_recycler_view.setAdapter(horizontalAdapter);

        horizontalList.clear();
        horizontalListFeedType.clear();

        for (int i = 0; i < mImageUriList.size(); i++) {
            Uri fileUri = mImageUriList.get(i);
            horizontalList.add(fileUri.toString());
            horizontalAdapter = new CustomAdapter(mContext, horizontalList, horizontalListFeedType);
            horizontal_recycler_view.setAdapter(horizontalAdapter);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_foreground);

            Glide.with(mContext).load(horizontalList.get(0)).apply(requestOptions).into(imagePreview);

            String path = getPath(mContext, fileUri);
            assert path != null;
            if (path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.endsWith(".avi")) {
                horizontalListFeedType.add(VIDEO);
            } else {
                horizontalListFeedType.add(IMAGE);
            }
        }

        backBtn.setOnClickListener(view -> {
            PreferenceHandler.getInstance(mContext).setFeedAddress("");
            PreferenceHandler.getInstance(mContext).setFeedDescription("");
            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");

            descriptionPost.setText("");
            mImageUriList.clear();
            finish();
        });

        postimage.setOnClickListener(view -> {
            CommonMember.showDialog(FeedPreview.this);
            for (int i = 0; i < mImageUriList.size(); i++) {
                Uri fileUri = mImageUriList.get(i);
                String path = getPath(mContext, fileUri);
                assert path != null;
                if (path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.endsWith(".avi")) {
                    mFeedType = VIDEO;
                } else {
                    mFeedType = IMAGE;
                }
                convertToString(fileUri);
            }
        });

        addImage.setOnClickListener(view -> {
            if (isNetworkConnected(mContext)) {
                CommonMember.showDialog(FeedPreview.this);
                showDialogForMedia();
            } else {
                CommonMember.dismissDialog();
                networkStatusAlert(mContext);
            }
        });

        tagImage.setOnClickListener(view -> {
            if (mUserArraylist != null) {
                if (mUserArraylist.size() > 0) {
                    showUserListDialog();
                } else {
                    Toast.makeText(mContext, getString(R.string.friends_not_found), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isNetworkConnected(mContext)) {
                    CommonMember.showDialog(FeedPreview.this);
                    if (mCloudDB == null) {
                        initiateTheCloud();
                    } else {
                        cloudDBZoneCreation();
                    }
                } else {
                    CommonMember.dismissDialog();
                    networkStatusAlert(mContext);
                }
            }
        });

        locationImage.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, LoadMapForFeed.class);
            startActivity(intent);
        });

        horizontal_recycler_view.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), horizontal_recycler_view, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_foreground);

                Glide.with(mContext).load(horizontalList.get(position)).apply(requestOptions).into(imagePreview);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void convertToString(Uri uri) {
        mMediaCommonId = getTimeStamp();
        String fileName;
        String mMediaId = getTimeStamp() + getTimeStamp();
        if (mFeedType.equals(IMAGE)) {
            fileName = mMediaId + ".jpg";
        } else {
            fileName = mMediaId + ".mp4";
        }

        StorageReference reference = storageManagement.getStorageReference(fileName);
        String path = getPath(mContext, uri);

        assert path != null;
        UploadTask task = reference.putFile(new File(path));
        task.addOnFailureListener(exception -> {
            CommonMember.dismissDialog();
            exception.getCause();
        }).addOnSuccessListener(uploadResult -> uploadResult.getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> insertMediaInDB(uri1.toString(), mMediaId)).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void insertMediaInDB(String uri, String mMediaId) {
        if (uri.contains(".mp4")) {
            mFeedType = VIDEO;
        } else {
            mFeedType = IMAGE;
        }
        Media mMediaDetail = new Media();
        mMediaDetail.setMediaId(mMediaId);
        mMediaDetail.setMediaCommonId(mMediaCommonId);
        mMediaDetail.setMediaURI(uri);
        mMediaDetail.setMediaType(mFeedType);
        upsertMediaInfos(mMediaDetail);
    }

    private void upsertMediaInfos(Media mediaDetail) {
        if (mCloudDBZone == null) {
            return;
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mediaDetail);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            String mFeedDescription = Objects.requireNonNull(descriptionPost.getText()).toString().trim();
            Feeds mFeedsDetail = new Feeds();
            mFeedsDetail.setFeedId(mMediaCommonId);
            mFeedsDetail.setUploadedBy(mUserName);
            mFeedsDetail.setUserImage(mUserImage);
            mFeedsDetail.setUploadedDate(getTimeStamp());
            mFeedsDetail.setFeedLikes("0");
            mFeedsDetail.setFeedUnlikes("0");
            mFeedsDetail.setMediaId(mediaDetail.getMediaCommonId());
            mFeedsDetail.setFeedDescription(mFeedDescription);

            upsertFeedsInfos(mFeedsDetail);

            HiAnalyticsInstance instance = HiAnalytics.getInstance(mContext);
            // Enable tracking of the custom event in proper positions of the code.
            Bundle bundle = new Bundle();
            bundle.putString(MEDIA_TYPE, mediaDetail.getMediaType());
            instance.onEvent(POSTED_FEED, bundle);

        }).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void upsertFeedsInfos(Feeds mFeedsDetail) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mFeedsDetail);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            mPostMediaCount++;
            if (mPostMediaCount == 1) {
                CommonMember.dismissDialog();

                Intent intent = new Intent(mContext, SplashAd.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }

            if (mImageUriList.size() == mPostMediaCount) {
                Toast.makeText(mContext, mContext.getString(R.string.feed_upload_success), Toast.LENGTH_SHORT).show();
                mImageUriList.clear();
                PreferenceHandler.getInstance(mContext).setFeedAddress("");
                PreferenceHandler.getInstance(mContext).setFeedDescription("");
                PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");

                descriptionPost.setText("");
                String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();

                PushApis mPushAPIs = new PushApis(mContext);
                mPushAPIs.sendPushNotification(mCurrentUserName + " has posted a new feed.", mUserTokenArraylist, POSTED_FEED);

                NotificationList mNotificationList = new NotificationList();
                mNotificationList.setNotificationId(mMediaCommonId);
                mNotificationList.setNotificationDate(mMediaCommonId);
                mNotificationList.setNotificationMessage(mCurrentUserName + " has posted a new feed.");
                mNotificationList.setNotificationType(NOTIFICATION_TYPE_FEED);
                mNotificationList.setNotificationTo(FEED_TO_ALL);
                mNotificationList.setNotificationUserId(PreferenceHandler.getInstance(mContext).getUserId());
                mNotificationList.setNotificationUserImage(PreferenceHandler.getInstance(mContext).getPhotoURL());

                insertNotificationInfo(mNotificationList);
            }
        }).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void insertNotificationInfo(NotificationList mNotificationListDetail) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mNotificationListDetail);
        upsertTask.addOnSuccessListener(command -> {
        }).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void showDialogForMedia() {
        final Dialog dialog = new Dialog(FeedPreview.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_media_layout);

        AppCompatImageView mCameraImage = dialog.findViewById(R.id.camera_image);
        AppCompatImageView mVideoImage = dialog.findViewById(R.id.video_image);
        AppCompatImageView mGalleryImage = dialog.findViewById(R.id.gallery_image);

        mCameraImage.setOnClickListener(v -> {
            dialog.dismiss();
            if (checkPermissionForCamera()) {
                cameraIntent();
            } else {
                // Request the permissions
                requestPermissionForCamera();
            }
        });

        mVideoImage.setOnClickListener(v -> {
            dialog.dismiss();
            if (checkPermissionForVideo()) {
                videoIntent();
            } else {
                // Request the permissions
                requestPermissionForVideo();
            }
        });

        mGalleryImage.setOnClickListener(v -> {
            dialog.dismiss();
            if (checkPermissionForGallery()) {
                galleryIntent();
            } else {
                // Request the permissions
                requestPermissionForGallery();
            }
        });


        dialog.show();
    }

    private void showDialogForFeeds(Intent data, int count) {
        if (count == 3) {
            if (data.getClipData() != null) {
                mData = data.getClipData();
                for (int i = 0; i < mData.getItemCount(); i++) {
                    ClipData.Item mItem = mData.getItemAt(i);
                    mImageUriList.add(mItem.getUri());
                }
            } else if (data.getData() != null) {
                mImageUriList.add(data.getData());
            } else {
                CommonMember.getErrorDialog(getResources().getString(R.string.media_not_valid), FeedPreview.this).show();
            }
        } else if (count == 2) {
            if (data.getData() != null) {
                mImageUriList.add(data.getData());
            } else {
                CommonMember.getErrorDialog(getResources().getString(R.string.media_not_valid), FeedPreview.this).show();
            }
        } else {
            mImageUriList.add(imageUri);
        }

        horizontalList.clear();
        horizontalListFeedType.clear();

        for (int i = 0; i < mImageUriList.size(); i++) {
            Uri fileUri = mImageUriList.get(i);
            horizontalList.add(fileUri.toString());

            String path = getPath(mContext, fileUri);
            assert path != null;
            if (path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.endsWith(".avi")) {
                horizontalListFeedType.add(VIDEO);
            } else {
                horizontalListFeedType.add(IMAGE);
            }
        }
        horizontalAdapter = new CustomAdapter(mContext, horizontalList, horizontalListFeedType);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(FeedPreview.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        horizontal_recycler_view.setAdapter(horizontalAdapter);
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
            int videoSeconds = 30;
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, videoSeconds);
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

    // Show user list dialog for notes
    private void showUserListDialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(FeedPreview.this);

        mSelectedUserList.clear();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line);
        arrayAdapter.addAll(mUserArraylist);

        builderSingle.setNegativeButton(getResources().getString(R.string.close), (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, i) -> {
            String mUserName1 = mUserArraylist.get(i);
            mSelectedUserList.add(mUserName1);

            String mDisplayName = PreferenceHandler.getInstance(mContext).getDisplayName();
            String mAddress = PreferenceHandler.getInstance(mContext).getFeedAddress();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < mSelectedUserList.size(); j++) {
                if (mSelectedUserList.size() > 1 && j != 0) {
                    sb.append(",");
                }
                sb.append(mSelectedUserList.get(j));
            }

            String mMsg;
            if (PreferenceHandler.getInstance(mContext).getFeedTaggedFriends().isEmpty()) {
                mMsg = mDisplayName + " with " + sb.toString();
            } else {
                StringBuilder sb1 = new StringBuilder();
                mMsg = PreferenceHandler.getInstance(mContext).getFeedTaggedFriends();
                mMsg = sb1.append(",").append(mMsg).toString();
            }

            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends(mMsg);

            if (!PreferenceHandler.getInstance(mContext).getFeedAddress().isEmpty()) {
                mMsg = mMsg + " in " + mAddress;
            }
            descriptionPost.setText(mMsg);
            PreferenceHandler.getInstance(mContext).setFeedDescription(mMsg);
        });
        builderSingle.show();

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
            initCloudStorage();
        } else {
            CommonMember.dismissDialog();
            networkStatusAlert(mContext);
        }
    }

    private void initCloudStorage() {
        storageManagement = AGCStorageManagement.getInstance();
        AGConnectOptions cnOptions = new AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.SINGAPORE).build(mContext);
        AGConnectInstance cnInstance = AGConnectInstance.buildInstance(cnOptions);
        storageManagement = AGCStorageManagement.getInstance(cnInstance, STORAGE_ID);
    }

    private void cloudDBZoneCreation() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(DBNAME, CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE, CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {
            mCloudDBZone = cloudDBZone;queryAllUser();
        }).addOnFailureListener(e -> CommonMember.dismissDialog());
    }

    private void queryAllUser() {
        if (mCloudDBZone == null) {
            CommonMember.dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(CloudDBZoneQuery.where(Users.class), CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {
            mUserArraylist = new ArrayList<>();
            mUserTokenArraylist = new ArrayList<>();

            mUserTotalListFromServer = snapshot.getSnapshotObjects().size();
            for (int i = 0; i < mUserTotalListFromServer; i++) {
                try {
                    String mUserName1 = snapshot.getSnapshotObjects().get(i).getUserName();
                    String mUserId = snapshot.getSnapshotObjects().get(i).getUserId();
                    String mToken = snapshot.getSnapshotObjects().get(i).getToken();

                    mUserTokenArraylist.add(mToken);

                    if (!mUserId.equals(PreferenceHandler.getInstance(mContext).getUserId())) {
                        mUserArraylist.add(mUserName1);
                    }

                } catch (AGConnectCloudDBException e) {
                    CommonMember.dismissDialog();
                    LogUtil.e("Res error : ", e.getMessage());
                }
            }
            CommonMember.dismissDialog();
        }).addOnFailureListener(e -> CommonMember.dismissDialog());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdLoad) {
            mAdLoad = false;
            finish();
        } else {
            String mMsg = PreferenceHandler.getInstance(mContext).getFeedDescription();
            descriptionPost.setText(mMsg);
            if (isNetworkConnected(mContext)) {
                CommonMember.showDialog(FeedPreview.this);
                if (mCloudDB == null) {
                    initiateTheCloud();
                } else {
                    cloudDBZoneCreation();
                }
            } else {
                CommonMember.dismissDialog();
                networkStatusAlert(mContext);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PreferenceHandler.getInstance(mContext).setFeedAddress("");
        PreferenceHandler.getInstance(mContext).setFeedDescription("");
        PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
        descriptionPost.setText("");
        mImageUriList.clear();
        finish();
    }
}
