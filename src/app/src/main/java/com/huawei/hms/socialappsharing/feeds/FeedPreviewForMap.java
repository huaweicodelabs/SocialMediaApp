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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
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

import static com.huawei.hms.socialappsharing.feeds.LoadMapForFeedPost.imageUri;
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

/**
 * This class refers the Feed Preview for the Map feed
 */
public class FeedPreviewForMap extends AppCompatActivity {

    private Context mContext;
    private AppCompatEditText descriptionPost;
    private AGCStorageManagement storageManagement;
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZone mCloudDBZone;
    private int mUserTotalListFromServer;
    private ArrayList<String> mUserArraylist;
    private ArrayList<String> mUserTokenArraylist;
    private final ArrayList<String> mSelectedUserList = new ArrayList<>();
    private String mMediaCommonId;
    private String mUserName;
    private String mUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_preview_map_layout);

        mContext = getApplicationContext();
        mUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
        mUserImage = PreferenceHandler.getInstance(mContext).getPhotoURL();

        mAdLoad = false;
        initiateTheCloud();

        ImageView backBtn = findViewById(R.id.back_btn);
        AppCompatButton tagImage = findViewById(R.id.tag_image);

        AppCompatButton postimage = findViewById(R.id.post_image);
        descriptionPost = findViewById(R.id.description_post);

        String mMsg = PreferenceHandler.getInstance(mContext).getFeedDescription();

        descriptionPost.setText(mMsg);

        AppCompatImageView imagePreview = findViewById(R.id.image_preview);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> Glide.with(mContext).load(imageUri).into(imagePreview), 3000);
        backBtn.setOnClickListener(view -> {
            PreferenceHandler.getInstance(mContext).setFeedAddress("");
            PreferenceHandler.getInstance(mContext).setFeedDescription("");
            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
            PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
            PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");

            descriptionPost.setText("");
            mImageUriList.clear();
            finish();
        });

        postimage.setOnClickListener(view -> {
            CommonMember.showDialog(FeedPreviewForMap.this);
            convertToString(imageUri);
        });


        tagImage.setOnClickListener(view -> {
            if(mUserArraylist!=null){
                if(mUserArraylist.size() > 0){
                    showUserListDialog();
                }else{
                    Toast.makeText(mContext, getString(R.string.friends_not_found), Toast.LENGTH_SHORT).show();
                }
            }else{
                if(isNetworkConnected(mContext)){
                    CommonMember.showDialog(FeedPreviewForMap.this);
                    if(mCloudDB == null){
                        initiateTheCloud();
                    }else{
                        cloudDBZoneCreation();
                    }
                }else {
                    CommonMember.dismissDialog();
                    networkStatusAlert(mContext);
                }
            }
        });
    }

    private void convertToString(Uri uri) {
        mMediaCommonId = getTimeStamp();
        String fileName;
        String mMediaId = getTimeStamp()+ getTimeStamp();

        fileName = mMediaId+".jpg";

        StorageReference reference = storageManagement.getStorageReference(fileName);
        String path = getPath(mContext,uri);

        assert path != null;
        UploadTask task = reference.putFile(new File(path));
        task.addOnFailureListener(exception -> {
            CommonMember.dismissDialog();
            exception.getCause();
        }).addOnSuccessListener(uploadResult -> uploadResult.getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> insertMediaInDB(uri1.toString(),mMediaId)).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void insertMediaInDB(String uri, String mMediaId) {
        String mFeedType = IMAGE;
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

            Toast.makeText(mContext, mContext.getString(R.string.feed_upload_success), Toast.LENGTH_SHORT).show();
            mImageUriList.clear();
            PreferenceHandler.getInstance(mContext).setFeedAddress("");
            PreferenceHandler.getInstance(mContext).setFeedDescription("");
            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends("");
            PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
            PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");

            descriptionPost.setText("");
            String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();

            File file = new File(imageUri.getPath());
            try{
                if (file.exists()) {
                    file.delete();
                }
            }catch (SecurityException e) {
                    LogUtil.e("Res error : " , e.getMessage());
            }

            PushApis mPushAPIs = new PushApis(mContext);
            mPushAPIs.sendPushNotification(mCurrentUserName+" has posted a new feed.",mUserTokenArraylist,POSTED_FEED);

            NotificationList mNotificationList = new NotificationList();
            mNotificationList.setNotificationId(mMediaCommonId);
            mNotificationList.setNotificationDate(mMediaCommonId);
            mNotificationList.setNotificationMessage(mCurrentUserName+" has posted a new feed.");
            mNotificationList.setNotificationType(NOTIFICATION_TYPE_FEED);
            mNotificationList.setNotificationTo(FEED_TO_ALL);
            mNotificationList.setNotificationUserId(PreferenceHandler.getInstance(mContext).getUserId());
            mNotificationList.setNotificationUserImage(PreferenceHandler.getInstance(mContext).getPhotoURL());

            insertNotificationInfo(mNotificationList);

            CommonMember.dismissDialog();

            Intent intent = new Intent(mContext, SplashAd.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

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
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
        }).addOnFailureListener(e -> {
            CommonMember.dismissDialog();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Show user list dialog for notes
    private void showUserListDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(FeedPreviewForMap.this);
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
            for (int j =0;j<mSelectedUserList.size();j++) {
                if(mSelectedUserList.size()>1 && j!=0){
                    sb.append(",");
                }
                sb.append(mSelectedUserList.get(j));
            }

            String mMsg;
            if(PreferenceHandler.getInstance(mContext).getFeedTaggedFriends().isEmpty()){
                mMsg = mDisplayName+" with "+sb.toString();
            }else{
                StringBuilder sb1 = new StringBuilder();
                mMsg = PreferenceHandler.getInstance(mContext).getFeedTaggedFriends();
                mMsg = sb1.append(",").append(mMsg).toString();
            }

            PreferenceHandler.getInstance(mContext).setFeedTaggedFriends(mMsg);

            if(!PreferenceHandler.getInstance(mContext).getFeedAddress().isEmpty()){
                mMsg = mMsg+" in "+mAddress;
            }

            descriptionPost.setText(mMsg);
            PreferenceHandler.getInstance(mContext).setFeedDescription(mMsg);
        });
        builderSingle.show();

    }

    private void initiateTheCloud() {
        if(isNetworkConnected(mContext)){
            AGConnectCloudDB.initialize(mContext);
            mCloudDB = AGConnectCloudDB.getInstance();

            try {
                mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            } catch (AGConnectCloudDBException e) {
                    LogUtil.e("Res error : " , e.getMessage());
            }
            cloudDBZoneCreation();
            initCloudStorage();
        }else {
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
            mCloudDBZone = cloudDBZone;
            queryAllUser();
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
            for(int i=0;i<mUserTotalListFromServer;i++){
                try {
                    String mUserName1 = snapshot.getSnapshotObjects().get(i).getUserName();
                    String mUserId = snapshot.getSnapshotObjects().get(i).getUserId();
                    String mToken = snapshot.getSnapshotObjects().get(i).getToken();

                    mUserTokenArraylist.add(mToken);

                    if(!mUserId.equals(PreferenceHandler.getInstance(mContext).getUserId())){
                        mUserArraylist.add(mUserName1);
                    }
                } catch (AGConnectCloudDBException e) {
                    CommonMember.dismissDialog();
                        LogUtil.e("Res error : " , e.getMessage());
                }
            }
            CommonMember.dismissDialog();
        }).addOnFailureListener(e -> CommonMember.dismissDialog());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdLoad){
            finish();
        }else{
            String mMsg = PreferenceHandler.getInstance(mContext).getFeedDescription();
            descriptionPost.setText(mMsg);

            if(isNetworkConnected(mContext)){
                CommonMember.showDialog(FeedPreviewForMap.this);
                if(mCloudDB == null){
                    initiateTheCloud();
                }else{
                    cloudDBZoneCreation();
                }
            }else {
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
        PreferenceHandler.getInstance(mContext).setFeedMapLatitude("");
        PreferenceHandler.getInstance(mContext).setFeedMapLongtitude("");
        descriptionPost.setText("");
        mImageUriList.clear();
        finish();
    }
}
