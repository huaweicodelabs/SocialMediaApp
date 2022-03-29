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

package com.huawei.hms.socialappsharing.notifications;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.TabActivity;
import com.huawei.hms.socialappsharing.models.NotificationList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.main.NotificationFragment.refreshNotifications;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getDate;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FEED;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST;


/**
 * This class refers the adapter for Notification list
 */
public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private final ArrayList<NotificationModel> mNotificationModelArraylist;
    private final FragmentActivity activity;
    private static CloudDBZone mCloudDBZone = null;

    public NotificationsListAdapter(Context mContext, FragmentActivity mActivity, ArrayList<NotificationModel> mNotificationModelArraylist, CloudDBZone mCloudDBZone) {
        NotificationsListAdapter.mContext = mContext;
        this.mNotificationModelArraylist = mNotificationModelArraylist;
        this.activity = mActivity;
        NotificationsListAdapter.mCloudDBZone = mCloudDBZone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mNotificationModelArraylist.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView imageView;
        public AppCompatImageView deleteItem;
        public AppCompatTextView msgTextView;
        public AppCompatTextView dateTextView;
        public LinearLayout detailLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.profile_image);
            this.deleteItem = itemView.findViewById(R.id.delete_image);
            this.msgTextView = itemView.findViewById(R.id.user_name_textView);
            this.dateTextView = itemView.findViewById(R.id.date_textView);
            this.detailLayout = itemView.findViewById(R.id.detail_layout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NotificationModel mNotificationDetails = mNotificationModelArraylist.get(position);
        String mPhotoURL = PHOTO_MAIN_URL + mNotificationDetails.getNotificationUserImage();
        Glide.with(mContext).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);

        holder.msgTextView.setText(mNotificationDetails.getNotificationMessage());

        String mDate = mNotificationDetails.getNotificationDate();
        holder.dateTextView.setText(getDate(mDate));

        holder.deleteItem.setOnClickListener(view -> showDialogForDelete(position));

        holder.detailLayout.setOnClickListener(view -> {
            NotificationModel mNotificationDetails1 = mNotificationModelArraylist.get(position);

            Intent intent = new Intent(mContext, TabActivity.class);

            if (mNotificationDetails1.getNotificationType().equals(NOTIFICATION_TYPE_FEED)) {
                intent.putExtra(NOTIFICATION_ID, NOTIFICATION_TYPE_FEED);
            } else {
                intent.putExtra(NOTIFICATION_ID, NOTIFICATION_TYPE_FRIEND_REQUEST);
            }
            mContext.startActivity(intent);
        });
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
                NotificationModel mNotificationDetails = mNotificationModelArraylist.get(position);
                showDialog(activity);
                CloudDBZoneQuery<NotificationList> mNotificationQuery = CloudDBZoneQuery.where(NotificationList.class).equalTo(NOTIFICATION_ID, mNotificationDetails.getNotificationId());
                queryNotification(mNotificationQuery);
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

    private static void queryNotification(CloudDBZoneQuery<NotificationList> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<NotificationList>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(NotificationsListAdapter::processQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private static void processQueryResult(CloudDBZoneSnapshot<NotificationList> snapshot) {
        CloudDBZoneObjectList<NotificationList> notificationInfoCursor = snapshot.getSnapshotObjects();
        try {
            for (int i = 0; i < notificationInfoCursor.size(); i++) {
                List<NotificationList> mNotificationList = Collections.singletonList(notificationInfoCursor.next());
                deleteNotificationInfoListAsync(mNotificationList);
            }
        } catch (AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    // bookInfoList is the dataset returned by the query operation or listener.
    private static void deleteNotificationInfoListAsync(List<NotificationList> notificationInfoList) {
        if (mCloudDBZone == null) {
            return;
        }
        Task<Integer> deleteTask = mCloudDBZone.executeDelete(notificationInfoList);
        deleteTask.addOnSuccessListener(integer -> {
            Toast.makeText(mContext, mContext.getString(R.string.notification_delete_success), Toast.LENGTH_SHORT).show();
            refreshNotifications();
        }).addOnFailureListener(e -> {
            dismissDialog();
            Toast.makeText(mContext, mContext.getString(R.string.unable_to_process_your_request), Toast.LENGTH_SHORT).show();
        });
    }
}