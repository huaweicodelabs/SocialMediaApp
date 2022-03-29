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
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.agconnect.AGCRoutePolicy;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptions;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.nativead.MediaView;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.models.Advertise;
import com.huawei.hms.socialappsharing.models.FeedModel;
import com.huawei.hms.socialappsharing.models.Feeds;
import com.huawei.hms.socialappsharing.models.Media;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.util.Collections;
import java.util.List;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.main.FeedFragment.refreshFeeds;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getDate;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.FEED_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_COMMON_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.STORAGE_ID;

/**
 * This class refers the Feed Parent adapter to structure the recyclerview items
 */
public class FeedsParentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static FragmentActivity activity;
    private static int mDeleteMediaCount = 0;
    private static int mMediaCountServer = 0;

    // An object of RecyclerView.RecycledViewPool
    // is created to share the Views
    // between the child and
    // the parent RecyclerViews

    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final List<Object> itemList;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private static CloudDBZone mCloudDBZone = null;
    private static int mAction;
    private final static int FEEDSLIST = 1;
    private final static int ADVERTISE = 2;

    public FeedsParentAdapter(Context mContext, List<Object> itemList, CloudDBZone mCloudDBZone, FragmentActivity activity) {
        this.itemList = itemList;
        FeedsParentAdapter.mContext = mContext;
        FeedsParentAdapter.mCloudDBZone = mCloudDBZone;
        FeedsParentAdapter.activity = activity;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof FeedModel) {
            return FEEDSLIST;
        } else if (itemList.get(position) instanceof Advertise) {
            try {
                return ADVERTISE;
            } catch (IndexOutOfBoundsException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Here we inflate the corresponding
        // layout of the parent item
        View view;
        switch (viewType) {
            case FEEDSLIST:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.parent_item, viewGroup, false);
                return new ParentViewHolder(view);
            case ADVERTISE:
                try {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nativesmall_ad_layout, viewGroup, false);
                    return new AdViewHolder(view);
                } catch (IndexOutOfBoundsException e) {
                    LogUtil.e("Res error : ", e.getMessage());
                }
        }
        return null;
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {

        int type = viewHolder.getItemViewType();

        switch (type) {
            case FEEDSLIST:
                ParentViewHolder parentViewHolder = (ParentViewHolder) viewHolder;
                FeedModel parentItem = (FeedModel) itemList.get(position);
                parentViewHolder.ParentItemTitle.setText(parentItem.getFeedDescription());

                parentViewHolder.mUserName.setText(parentItem.getUploadedBy());
                parentViewHolder.mLikeCount.setText(parentItem.getFeedLikes());
                parentViewHolder.mUnlikeCount.setText(parentItem.getFeedUnLikes());

                String mDate = parentItem.getUploadedDate();
                parentViewHolder.mDateView.setText(getDate(mDate));

                if (parentItem.getUploadedBy().equals(PreferenceHandler.getInstance(mContext).getDisplayName())) {
                    parentViewHolder.menuBtn.setVisibility(View.VISIBLE);
                } else {
                    parentViewHolder.menuBtn.setVisibility(View.GONE);
                }

                String mUrl = parentItem.getUserImage();
                String mPhotoURL = PHOTO_MAIN_URL + mUrl;
                if (parentItem.getUserImage() != null) {
                    Glide.with(mContext).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(parentViewHolder.mProfileImage);
                }

                parentViewHolder.mLikeBtn.setOnClickListener(view -> {
                    int mCount = Integer.parseInt(String.valueOf(parentViewHolder.mLikeCount.getText())) + 1;
                    parentViewHolder.mLikeCount.setText(String.valueOf(mCount));
                    CloudDBZoneQuery<Feeds> mFeedQuery = CloudDBZoneQuery.where(Feeds.class).equalTo(FEED_ID, parentItem.getFeedId());
                    queryFeedLikes(mFeedQuery);
                    mAction = 0;
                });

                parentViewHolder.mUnLikeBtn.setOnClickListener(view -> {
                    int mCount = Integer.parseInt(String.valueOf(parentViewHolder.mUnlikeCount.getText())) + 1;
                    parentViewHolder.mUnlikeCount.setText(String.valueOf(mCount));
                    CloudDBZoneQuery<Feeds> mFeedQuery = CloudDBZoneQuery.where(Feeds.class).equalTo(FEED_ID, parentItem.getFeedId());
                    queryFeedLikes(mFeedQuery);
                    mAction = 1;
                });

                parentViewHolder.menuBtn.setOnClickListener(view -> showDialogForDelete(position));

                // Create a layout manager
                // to assign a layout
                // to the RecyclerView.
                GridLayoutManager gridLayout;

                FeedsChildAdapter childItemAdapter = new FeedsChildAdapter(mContext, parentItem.getArrayList());

                if (((FeedModel) itemList.get(position)).getArrayList().size() >= 2) {
                    gridLayout = new GridLayoutManager(parentViewHolder.ChildRecyclerView.getContext(), 2);
                } else {
                    gridLayout = new GridLayoutManager(parentViewHolder.ChildRecyclerView.getContext(), 1);
                }
                gridLayout.setInitialPrefetchItemCount(parentItem.getArrayList().size());
                parentViewHolder.ChildRecyclerView.setLayoutManager(gridLayout);
                parentViewHolder.ChildRecyclerView.setAdapter(childItemAdapter);
                parentViewHolder.ChildRecyclerView.setRecycledViewPool(viewPool);
                break;
            case ADVERTISE:
                AdViewHolder adViewHolder = (AdViewHolder) viewHolder;
                Advertise advertise = (Advertise) itemList.get(position);
                loadNativeAd(adViewHolder, advertise);
                break;
        }

    }

    private void loadNativeAd(final AdViewHolder myAdViewHolder, Advertise advertisemodel) {
        NativeAdLoader.Builder builder = new NativeAdLoader.Builder(mContext, advertisemodel.getAdID());
        builder.setNativeAdLoadedListener(nativeAd -> {
            // Call this method when an ad is successfully loaded.
            // Display native ad.
            showNativeAd(nativeAd, myAdViewHolder);
            nativeAd.setDislikeAdListener(() -> {
                // Call this method when an ad is closed.
            });
        }).setAdListener(new AdListener() {
            @Override
            public void onAdFailed(int errorCode) {
                // Call this method when an ad fails to be loaded.
            }
        });
        NativeAdConfiguration adConfiguration = new NativeAdConfiguration.Builder()
                .setChoicesPosition(NativeAdConfiguration.ChoicesPosition.TOP_RIGHT) // Set custom attributes.
                .build();
        NativeAdLoader nativeAdLoader = builder.setNativeAdOptions(adConfiguration).build();
        nativeAdLoader.loadAd(new AdParam.Builder().build());
    }

    private void showNativeAd(NativeAd nativeAd, AdViewHolder myAdViewHolder) {
        myAdViewHolder.txtTitle.setText(nativeAd.getTitle());
        myAdViewHolder.txtAdSource.setText(nativeAd.getAdSource());
        myAdViewHolder.btnAction.setText(nativeAd.getCallToAction());
        myAdViewHolder.nativeView.setMediaView(myAdViewHolder.mediaView);
        myAdViewHolder.nativeView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        myAdViewHolder.nativeView.setNativeAd(nativeAd);
    }


    // This class is to initialize
    // the Views present in
    // the parent RecyclerView
    static class ParentViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatButton menuBtn;
        private final AppCompatTextView ParentItemTitle;
        private final AppCompatTextView mUserName;
        private final AppCompatTextView mLikeCount;
        private final AppCompatTextView mUnlikeCount;
        private final AppCompatTextView mDateView;
        private final AppCompatImageView mProfileImage;
        private final AppCompatImageView mLikeBtn;
        private final AppCompatImageView mUnLikeBtn;
        private final RecyclerView ChildRecyclerView;

        ParentViewHolder(final View itemView) {
            super(itemView);
            ParentItemTitle = itemView.findViewById(R.id.parent_item_title);
            mUserName = itemView.findViewById(R.id.user_name_textView);
            mDateView = itemView.findViewById(R.id.date_textView);
            mProfileImage = itemView.findViewById(R.id.profile_image);
            menuBtn = itemView.findViewById(R.id.feed_menu_btn);
            mLikeBtn = itemView.findViewById(R.id.like_feed);
            mUnLikeBtn = itemView.findViewById(R.id.unlike_feed);

            mLikeCount = itemView.findViewById(R.id.like_count);
            mUnlikeCount = itemView.findViewById(R.id.unlike_count);

            ChildRecyclerView = itemView.findViewById(R.id.child_recyclerview);
        }
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        private final NativeView nativeView;
        private final MediaView mediaView;
        private final TextView txtTitle;
        private final TextView txtAdSource;
        private final Button btnAction;

        AdViewHolder(View view) {
            super(view);
            nativeView = view.findViewById(R.id.native_small_view);
            mediaView = view.findViewById(R.id.ad_media);
            txtTitle = view.findViewById(R.id.ad_title);
            txtAdSource = view.findViewById(R.id.ad_source);
            btnAction = view.findViewById(R.id.ad_call_to_action);
        }
    }

    private void showDialogForDelete(int position) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView mTitle = dialog.findViewById(R.id.txt_dia);
        Button yesButton = dialog.findViewById(R.id.btn_yes_dialog);
        Button noButton = dialog.findViewById(R.id.btn_no_dialog);
        EditText mDescriptionView = dialog.findViewById(R.id.edittext_dialog);

        mDescriptionView.setVisibility(View.GONE);
        mTitle.setText(mContext.getString(R.string.delete_feed));

        yesButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (isNetworkConnected(mContext)) {
                FeedModel parentItem = (FeedModel) itemList.get(position);
                mDeleteMediaCount = 0;
                showDialog(activity);
                CloudDBZoneQuery<Feeds> mFeedQuery = CloudDBZoneQuery.where(Feeds.class).equalTo(FEED_ID, parentItem.getFeedId());
                queryFeed(mFeedQuery);
            } else {
                networkStatusAlert(mContext);
            }
        });

        noButton.setOnClickListener(view -> {
            dialog.dismiss();
            dismissDialog();
        });
        dialog.show();
    }

    private static void queryFeed(CloudDBZoneQuery<Feeds> query) {
        if (mCloudDBZone == null) {
            dismissDialog();

            return;
        }
        Task<CloudDBZoneSnapshot<Feeds>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(FeedsParentAdapter::processQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private static void processQueryResult(CloudDBZoneSnapshot<Feeds> snapshot) {
        CloudDBZoneObjectList<Feeds> feedInfoCursor = snapshot.getSnapshotObjects();
        try {
            for (int i = 0; i < feedInfoCursor.size(); i++) {
                List<Feeds> mFeeds = Collections.singletonList(feedInfoCursor.next());
                deleteFeedAsync(mFeeds);

                String mMediaID = mFeeds.get(i).getMediaId();
                CloudDBZoneQuery<Media> mMediaQuery = CloudDBZoneQuery.where(Media.class).equalTo(MEDIA_COMMON_ID, mMediaID);
                queryMedia(mMediaQuery);
            }
        } catch (AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    // bookInfoList is the dataset returned by the query operation or listener.
    private static void deleteFeedAsync(List<Feeds> feedInfoList) {
        if (mCloudDBZone == null) {

            return;
        }

        Task<Integer> deleteTask = mCloudDBZone.executeDelete(feedInfoList);
        deleteTask.addOnSuccessListener(integer -> {
            Toast.makeText(mContext, mContext.getString(R.string.feed_delete_success), Toast.LENGTH_SHORT).show();
            refreshFeeds();
        }).addOnFailureListener(e -> {
            dismissDialog();
            Toast.makeText(mContext, mContext.getString(R.string.unable_to_process_your_request), Toast.LENGTH_SHORT).show();
        });
    }

    private static void queryMedia(CloudDBZoneQuery<Media> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Media>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(FeedsParentAdapter::processMediaQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private static void processMediaQueryResult(CloudDBZoneSnapshot<Media> snapshot) {
        CloudDBZoneObjectList<Media> mediaInfoCursor = snapshot.getSnapshotObjects();
        try {
            mMediaCountServer = mediaInfoCursor.size();
            for (int i = 0; i < mediaInfoCursor.size(); i++) {
                Media mMedia = mediaInfoCursor.get(i);

                AGConnectOptions cnOptions = new AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.SINGAPORE).build(mContext);
                AGConnectInstance cnInstance = AGConnectInstance.buildInstance(cnOptions);
                AGCStorageManagement storageManagement = AGCStorageManagement.getInstance(cnInstance, STORAGE_ID);

                StorageReference reference;
                if (mMedia.getMediaType().equals(IMAGE)) {
                    reference = storageManagement.getStorageReference(mMedia.getMediaId() + ".jpg");
                } else {
                    reference = storageManagement.getStorageReference(mMedia.getMediaId() + ".mp4");
                }
                reference.delete();
                deleteMediaAsync(mMedia);
            }
        } catch (AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    private static void deleteMediaAsync(Media mediaInfoList) {
        if (mCloudDBZone == null) {

            return;
        }

        Task<Integer> deleteTask = mCloudDBZone.executeDelete(mediaInfoList);
        deleteTask.addOnSuccessListener(integer -> {
            mDeleteMediaCount++;
            if (mMediaCountServer == mDeleteMediaCount) {
                dismissDialog();
                Toast.makeText(mContext, mContext.getString(R.string.media_delete_success), Toast.LENGTH_SHORT).show();
                refreshFeeds();
            }

        }).addOnFailureListener(e -> {
            dismissDialog();
            Toast.makeText(mContext, mContext.getString(R.string.unable_to_process_your_request), Toast.LENGTH_SHORT).show();
        });
    }


    private static void queryFeedLikes(CloudDBZoneQuery<Feeds> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Feeds>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(FeedsParentAdapter::processFeedLikeQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private static void processFeedLikeQueryResult(CloudDBZoneSnapshot<Feeds> snapshot) {
        CloudDBZoneObjectList<Feeds> userInfoCursor = snapshot.getSnapshotObjects();
        Feeds mFeedsDetail = new Feeds();
        try {
            while (userInfoCursor.hasNext()) {
                Feeds mFeeds = userInfoCursor.next();
                activity.runOnUiThread(() -> {
                    if (mAction == 0) {
                        int feedLikes = Integer.parseInt(mFeeds.getFeedLikes());
                        ++feedLikes;
                        mFeedsDetail.setFeedLikes(String.valueOf(feedLikes));
                        mFeedsDetail.setFeedUnlikes(mFeeds.getFeedUnlikes());

                    } else {
                        int feedLikes = Integer.parseInt(mFeeds.getFeedUnlikes());
                        ++feedLikes;
                        mFeedsDetail.setFeedUnlikes(String.valueOf(feedLikes));
                        mFeedsDetail.setFeedLikes(mFeeds.getFeedLikes());
                    }
                });
                mFeedsDetail.setFeedId(mFeeds.getFeedId());
                mFeedsDetail.setUploadedBy(mFeeds.getUploadedBy());
                mFeedsDetail.setUploadedDate(mFeeds.getUploadedDate());
                mFeedsDetail.setMediaId(mFeeds.getMediaId());
                mFeedsDetail.setFeedDescription(mFeeds.getFeedDescription());
                mFeedsDetail.setUserImage(mFeeds.getUserImage());
            }

            updateFeedsCount(mFeedsDetail);

        } catch (AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    // feedInfoList is the dataset returned by the query operation or listener.
    private static void updateFeedsCount(Feeds feedInfoList) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(feedInfoList);
        upsertTask.addOnFailureListener(e -> dismissDialog());
    }
}
