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

package com.huawei.hms.socialappsharing.friends;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getTimeStamp;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_LIST_REQUEST;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REQUESTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REQUESTED_LIST;
import static com.huawei.hms.socialappsharing.utils.Constants.NAME_VALUE_PAIRS;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST;
import static com.huawei.hms.socialappsharing.utils.Constants.USER_EMAIL;
import static com.huawei.hms.socialappsharing.utils.Constants.VALUES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.socialappsharing.pushmessage.PushApis;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.models.NotificationList;
import com.huawei.hms.socialappsharing.models.Users;
import com.huawei.hms.socialappsharing.utils.CommonMember;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class refers the adapter for friends Suggestions list
 */
public class FriendsSuggestionsListAdapter extends RecyclerView.Adapter<FriendsSuggestionsListAdapter.ViewHolder> {
    private final Context mContext;
    private final Activity mActivity;
    private final CloudDBZone mCloudDBZone;
    private final ArrayList<UserModel> mUserModelArraylist;
    private final ArrayList<String> mFriendRequestedArraylist;
    private String mCurrentUser;
    private String mFriendUser;

    public FriendsSuggestionsListAdapter(Context mContext, Activity mActivity, ArrayList<UserModel> mUserModelArraylist, CloudDBZone mCloudDBZone, ArrayList<String> mFriendRequestedArraylist) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mUserModelArraylist = mUserModelArraylist;
        this.mCloudDBZone = mCloudDBZone;
        this.mFriendRequestedArraylist = mFriendRequestedArraylist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }


    @Override
    public int getItemCount() {
        return mUserModelArraylist.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView imageView;
        public AppCompatTextView textView;
        public AppCompatButton addFriendBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.profile_image);
            this.textView = itemView.findViewById(R.id.user_name_textView);
            addFriendBtn = itemView.findViewById(R.id.add_friend_btn);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        UserModel mUserDetails = mUserModelArraylist.get(position);

        mCurrentUser = PreferenceHandler.getInstance(mContext).getEmailId();
        mFriendUser = mUserDetails.getUserEmail();

        String mPhotoURL = PHOTO_MAIN_URL + mUserDetails.getProfileImage();
        Glide.with(mContext).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);

        holder.textView.setText(mUserDetails.getUserName());

        for (String mFriend : mFriendRequestedArraylist) {
            if (mFriend.equals(mFriendUser)) {
                holder.addFriendBtn.setText(R.string.requested);
                holder.addFriendBtn.setClickable(false);
                holder.addFriendBtn.setEnabled(false);
            } else {
                holder.addFriendBtn.setText(R.string.request);
            }
        }

        holder.addFriendBtn.setOnClickListener(view -> {
            UserModel mUserDetails1 = mUserModelArraylist.get(position);
            mFriendUser = mUserDetails1.getUserEmail();
            if (isNetworkConnected(mContext)) {
                showDialog(mActivity);
                CloudDBZoneQuery<Users> mUserQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
                queryCurrentUser(mUserQuery);

                CloudDBZoneQuery<Users> mFriendQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mFriendUser);
                queryFriendUser(mFriendQuery);
            } else {
                networkStatusAlert(mContext);
            }
        });
    }

    private void queryCurrentUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processCurrentUserQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processCurrentUserQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();
        boolean mAlreadyExist = false;
        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();

                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(mUsers.getNoOfFriends());
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());
                mUserDetail.setFriendsList(mUsers.getFriendsList());
                mUserDetail.setFriendRequestList(mUsers.getFriendRequestList());
                String friendRequestedList = mUsers.getFriendRequestedList();

                JSONObject mObj = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                if (friendRequestedList == null) {
                    jsonArray.put(mFriendUser);
                } else {
                    JSONObject jsonRootObject = new JSONObject(friendRequestedList);
                    JSONObject jobj = jsonRootObject.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject jobj1 = jobj.getJSONObject(FRIENDS_REQUESTED_LIST);
                    JSONArray jobj2 = jobj1.getJSONArray(VALUES);

                    // Iterate the jsonArray and print the info of JSONObjects
                    for (int i = 0; i < jobj2.length(); i++) {
                        String mFriend = jobj2.getString(i);
                        jsonArray.put(mFriend);

                        if (mFriend.equals(mFriendUser)) {
                            mAlreadyExist = true;
                        }
                    }
                    if (!mAlreadyExist) {
                        jsonArray.put(mFriendUser);
                    }
                }
                mObj.put(FRIENDS_REQUESTED_LIST, jsonArray);

                Gson gson = new Gson();
                friendRequestedList = gson.toJson(mObj);
                mUserDetail.setFriendRequestedList(friendRequestedList);
            }

            upsertUsersInfos(mUserDetail);

        } catch (JSONException | AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    private void queryFriendUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processFriendQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processFriendQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();
        boolean mAlreadyExist = false;

        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();

                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(mUsers.getNoOfFriends());
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());
                mUserDetail.setFriendsList(mUsers.getFriendsList());
                mUserDetail.setFriendRequestedList(mUsers.getFriendRequestedList());

                String friendRequestList = mUsers.getFriendRequestList();
                JSONObject mObj = new JSONObject();
                JSONArray jsonArray = new JSONArray();


                if (friendRequestList == null) {
                    jsonArray.put(mCurrentUser);
                } else {
                    JSONObject jsonRootObject = new JSONObject(friendRequestList);
                    JSONObject jobj = jsonRootObject.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject jobj1 = jobj.getJSONObject(FRIENDS_LIST_REQUEST);
                    JSONArray jobj2 = jobj1.getJSONArray(VALUES);

                    // Iterate the jsonArray and print the info of JSONObjects
                    for (int i = 0; i < jobj2.length(); i++) {
                        String mFriend = jobj2.getString(i);
                        jsonArray.put(mFriend);

                        if (mFriend.equals(mCurrentUser)) {
                            mAlreadyExist = true;
                        }
                    }

                    if (!mAlreadyExist) {
                        jsonArray.put(mCurrentUser);
                    }
                }
                mObj.put(FRIENDS_LIST_REQUEST, jsonArray);

                Gson gson = new Gson();
                friendRequestList = gson.toJson(mObj);
                mUserDetail.setFriendRequestList(friendRequestList);

                ArrayList<String> mUserTokenArraylist = new ArrayList<>();
                mUserTokenArraylist.add(mUsers.getToken());

                String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
                PushApis mPushAPIs = new PushApis(mContext);
                mPushAPIs.sendPushNotification(mCurrentUserName + " has requested a Friend Request ", mUserTokenArraylist, FRIENDS_REQUESTED);
            }
            upsertUsersInfos(mUserDetail);

        } catch (JSONException | AGConnectCloudDBException jsonException) {
            dismissDialog();
            jsonException.printStackTrace();
        }
    }

    private void upsertUsersInfos(Users users) {
        if (mCloudDBZone == null) {

            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(users);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            FriendsSuggestionsListFragment.sendData();
            String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();

            NotificationList mNotificationList = new NotificationList();
            mNotificationList.setNotificationId(getTimeStamp());
            mNotificationList.setNotificationDate(getTimeStamp());
            mNotificationList.setNotificationMessage(mCurrentUserName + " has requested a Friend Request ");
            mNotificationList.setNotificationType(NOTIFICATION_TYPE_FRIEND_REQUEST);
            mNotificationList.setNotificationTo(mFriendUser);
            mNotificationList.setNotificationUserId(PreferenceHandler.getInstance(mContext).getUserId());
            mNotificationList.setNotificationUserImage(PreferenceHandler.getInstance(mContext).getPhotoURL());

            insertNotificationInfo(mNotificationList);

        }).addOnFailureListener(e -> dismissDialog());
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
}

