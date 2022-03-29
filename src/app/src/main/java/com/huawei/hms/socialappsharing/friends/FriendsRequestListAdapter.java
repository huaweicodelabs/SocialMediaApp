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
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_ACCEPTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_LIST;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_LIST_REQUEST;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REJECTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REQUESTED_LIST;
import static com.huawei.hms.socialappsharing.utils.Constants.NAME_VALUE_PAIRS;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_ACCEPTED;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_REJECTED;
import static com.huawei.hms.socialappsharing.utils.Constants.USER_EMAIL;
import static com.huawei.hms.socialappsharing.utils.Constants.VALUES;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.socialappsharing.pushmessage.PushApis;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.models.NotificationList;
import com.huawei.hms.socialappsharing.models.Users;
import com.huawei.hms.socialappsharing.utils.CommonMember;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class refers the adapter for friends request list
 */
public class FriendsRequestListAdapter extends RecyclerView.Adapter<FriendsRequestListAdapter.ViewHolder> {

    private final Context mContext;
    private final Activity mActivity;
    private final CloudDBZone mCloudDBZone;
    private final ArrayList<UserModel> mUserModelArraylist;
    private String mCurrentUser;
    private String mFriendUser;
    private String mNotificationMsg = null;

    public FriendsRequestListAdapter(Context mContext, Activity mActivity, ArrayList<UserModel> mUserModelArraylist, CloudDBZone mCloudDBZone) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mUserModelArraylist = mUserModelArraylist;
        this.mCloudDBZone = mCloudDBZone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_list_item, parent, false);
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
        public AppCompatButton removeFriendBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.profile_image);
            this.textView = itemView.findViewById(R.id.user_name_textView);
            addFriendBtn = itemView.findViewById(R.id.add_friend_btn);
            removeFriendBtn = itemView.findViewById(R.id.remove_friend_btn);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUserDetails = mUserModelArraylist.get(position);

        mCurrentUser = PreferenceHandler.getInstance(mContext).getEmailId();
        mFriendUser = mUserDetails.getUserEmail();

        String mPhotoURL = PHOTO_MAIN_URL + mUserDetails.getProfileImage();

        Glide.with(mContext).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);

        holder.textView.setText(mUserDetails.getUserName());

        holder.addFriendBtn.setText(mContext.getString(R.string.accept));

        holder.addFriendBtn.setOnClickListener(view -> {

            if (isNetworkConnected(mContext)) {
                showDialog(mActivity);
                CloudDBZoneQuery<Users> mUserQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
                queryAddCurrentUser(mUserQuery);

                CloudDBZoneQuery<Users> mFriendQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mFriendUser);
                queryAddFriendUser(mFriendQuery);
            } else {
                dismissDialog();
                networkStatusAlert(mContext);
            }
        });


        holder.removeFriendBtn.setOnClickListener(view -> {
            if (isNetworkConnected(mContext)) {
                showDialog(mActivity);
                CloudDBZoneQuery<Users> mUserQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
                queryRemoveCurrentUser(mUserQuery);

                CloudDBZoneQuery<Users> mFriendQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mFriendUser);
                queryRemoveFriendUser(mFriendQuery);
            } else {
                dismissDialog();
                networkStatusAlert(mContext);
            }
        });
    }

    private void queryAddCurrentUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processAddCurrentUserQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processAddCurrentUserQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();
        boolean mAlreadyExist = false;

        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();

                String mFriendRequestList = mUsers.getFriendRequestList();

                JSONObject mObj1 = new JSONObject();

                JSONObject jsonFriendRequestList = new JSONObject(mFriendRequestList);
                JSONObject jobjFriendRequestList = jsonFriendRequestList.getJSONObject(NAME_VALUE_PAIRS);
                JSONObject jobjFriendRequestList1 = jobjFriendRequestList.getJSONObject(FRIENDS_LIST_REQUEST);
                JSONArray jobjFriendRequestList2 = jobjFriendRequestList1.getJSONArray(VALUES);

                // Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jobjFriendRequestList2.length(); i++) {
                    String mFriend = jobjFriendRequestList2.getString(i);
                    if (mFriend.equals(mFriendUser)) {
                        jobjFriendRequestList2.remove(i);
                        break;
                    }
                }

                mObj1.put(FRIENDS_LIST_REQUEST, jobjFriendRequestList2);
                Gson gson = new Gson();
                String mFriendRequestList1 = gson.toJson(mObj1);
                mUserDetail.setFriendRequestList(mFriendRequestList1);

                int friendsCount = Integer.parseInt(mUsers.getNoOfFriends());
                String mFriendList = mUsers.getFriendsList();

                JSONObject mObj = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                if (mFriendList == null) {
                    jsonArray.put(mFriendUser);
                    mObj.put(FRIENDS_LIST, jsonArray);
                    ++friendsCount;
                } else {
                    JSONObject jsonRootObject = new JSONObject(mFriendList);
                    JSONObject jobj = jsonRootObject.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject jobj1 = jobj.getJSONObject(FRIENDS_LIST);
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
                        ++friendsCount;

                    }
                    mObj.put(FRIENDS_LIST, jsonArray);
                }

                Gson gson1 = new Gson();
                mFriendList = gson1.toJson(mObj);

                mUserDetail.setFriendsList(mFriendList);

                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(String.valueOf(friendsCount));
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());
                mUserDetail.setFriendRequestedList(mUsers.getFriendRequestedList());
            }
            addfriendBack(mUserDetail);
        } catch (AGConnectCloudDBException | JSONException e) {
            LogUtil.e("Res error : ", e.getMessage());
        }
    }

    private void addfriendBack(Users users) {
        if (mCloudDBZone == null) {

            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(users);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            FriendsRequestListFragment.sendData();

            HiAnalyticsInstance instance = HiAnalytics.getInstance(mContext);
            // Enable tracking of the custom event in proper positions of the code.
            Bundle bundle = new Bundle();
            bundle.putString(users.getUserName(), users.getNoOfFriends());
            instance.onEvent("Friends-List", bundle);

            if (mNotificationMsg != null) {
                NotificationList mNotificationList = new NotificationList();
                mNotificationList.setNotificationId(getTimeStamp());
                mNotificationList.setNotificationDate(getTimeStamp());
                mNotificationList.setNotificationMessage(mNotificationMsg);
                mNotificationList.setNotificationTo(mFriendUser);
                mNotificationList.setNotificationUserId(PreferenceHandler.getInstance(mContext).getUserId());
                mNotificationList.setNotificationUserImage(PreferenceHandler.getInstance(mContext).getPhotoURL());

                if (mNotificationMsg.contains("rejected")) {
                    mNotificationList.setNotificationType(NOTIFICATION_TYPE_FRIEND_REJECTED);
                } else {
                    mNotificationList.setNotificationType(NOTIFICATION_TYPE_FRIEND_ACCEPTED);
                }
                insertNotificationInfo(mNotificationList);
            }
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


    private void queryAddFriendUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processAddFriendUserQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processAddFriendUserQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();
        boolean mAlreadyExist = false;

        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();

                String mFriendRequestedList = mUsers.getFriendRequestedList();

                JSONObject mObj1 = new JSONObject();

                JSONObject jsonFriendRequestList = new JSONObject(mFriendRequestedList);
                JSONObject jobjFriendRequestList = jsonFriendRequestList.getJSONObject(NAME_VALUE_PAIRS);
                JSONObject jobjFriendRequestList1 = jobjFriendRequestList.getJSONObject(FRIENDS_REQUESTED_LIST);
                JSONArray jobjFriendRequestList2 = jobjFriendRequestList1.getJSONArray(VALUES);

                // Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jobjFriendRequestList2.length(); i++) {
                    String mFriend = jobjFriendRequestList2.getString(i);
                    if (mFriend.equals(mCurrentUser)) {
                        jobjFriendRequestList2.remove(i);
                        break;
                    }
                }
                mObj1.put(FRIENDS_REQUESTED_LIST, jobjFriendRequestList2);

                Gson gson = new Gson();
                String mFriendRequestedList1 = gson.toJson(mObj1);
                mUserDetail.setFriendRequestedList(mFriendRequestedList1);

                int friendsCount = Integer.parseInt(mUsers.getNoOfFriends());
                String mFriendList = mUsers.getFriendsList();

                JSONObject mObj = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                if (mFriendList == null) {
                    jsonArray.put(mCurrentUser);
                    mObj.put(FRIENDS_LIST, jsonArray);
                    ++friendsCount;
                } else {
                    JSONObject jsonRootObject = new JSONObject(mFriendList);
                    JSONObject jobj = jsonRootObject.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject jobj1 = jobj.getJSONObject(FRIENDS_LIST);
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
                        ++friendsCount;

                    }
                    mObj.put(FRIENDS_LIST, jsonArray);
                }

                Gson gson1 = new Gson();
                mFriendList = gson1.toJson(mObj);
                mUserDetail.setFriendsList(mFriendList);

                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(String.valueOf(friendsCount));
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());

                mUserDetail.setFriendRequestList(mUsers.getFriendRequestList());

                ArrayList<String> mUserTokenArraylist = new ArrayList<>();
                mUserTokenArraylist.add(mUsers.getToken());

                String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
                PushApis mPushAPIs = new PushApis(mContext);
                mPushAPIs.sendPushNotification(mCurrentUserName + " has accepted a Friend Request ", mUserTokenArraylist, FRIENDS_ACCEPTED);

                mNotificationMsg = mCurrentUserName + " has accepted a Friend Request ";
            }
            addfriendBack(mUserDetail);
        } catch (AGConnectCloudDBException | JSONException e) {
            dismissDialog();
            LogUtil.e("Res error : ", e.getMessage());
        }
    }

    private void queryRemoveCurrentUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processRemoveCurrentUserQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processRemoveCurrentUserQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();

        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();

                String mFriendRequestList = mUsers.getFriendRequestList();

                JSONObject mObj1 = new JSONObject();

                JSONObject jsonFriendRequestList = new JSONObject(mFriendRequestList);
                JSONObject jobjFriendRequestList = jsonFriendRequestList.getJSONObject(NAME_VALUE_PAIRS);
                JSONObject jobjFriendRequestList1 = jobjFriendRequestList.getJSONObject(FRIENDS_LIST_REQUEST);
                JSONArray jobjFriendRequestList2 = jobjFriendRequestList1.getJSONArray(VALUES);

                // Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jobjFriendRequestList2.length(); i++) {
                    String mFriend = jobjFriendRequestList2.getString(i);
                    if (mFriend.equals(mFriendUser)) {
                        jobjFriendRequestList2.remove(i);
                        break;
                    }
                }
                mObj1.put(FRIENDS_LIST_REQUEST, jobjFriendRequestList2);

                Gson gson = new Gson();
                String mFriendRequestList1 = gson.toJson(mObj1);
                mUserDetail.setFriendRequestList(mFriendRequestList1);

                mUserDetail.setFriendsList(mUsers.getFriendsList());
                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(mUsers.getNoOfFriends());
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());
                mUserDetail.setFriendRequestedList(mUsers.getFriendRequestedList());
            }
            addfriendBack(mUserDetail);
        } catch (AGConnectCloudDBException | JSONException e) {
            dismissDialog();
            LogUtil.e("Res error : ", e.getMessage());
        }
    }


    private void queryRemoveFriendUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::processRemoveFriendUserQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void processRemoveFriendUserQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();

        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();
                String mFriendRequestedList = mUsers.getFriendRequestedList();

                JSONObject mObj1 = new JSONObject();

                JSONObject jsonFriendRequestList = new JSONObject(mFriendRequestedList);
                JSONObject jobjFriendRequestList = jsonFriendRequestList.getJSONObject(NAME_VALUE_PAIRS);
                JSONObject jobjFriendRequestList1 = jobjFriendRequestList.getJSONObject(FRIENDS_REQUESTED_LIST);
                JSONArray jobjFriendRequestList2 = jobjFriendRequestList1.getJSONArray(VALUES);


                // Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jobjFriendRequestList2.length(); i++) {
                    String mFriend = jobjFriendRequestList2.getString(i);
                    if (mFriend.equals(mCurrentUser)) {
                        jobjFriendRequestList2.remove(i);
                        break;
                    }
                }
                mObj1.put(FRIENDS_REQUESTED_LIST, jobjFriendRequestList2);

                Gson gson = new Gson();
                String mFriendRequestedList1 = gson.toJson(mObj1);

                mUserDetail.setFriendRequestedList(mFriendRequestedList1);

                mUserDetail.setFriendsList(mUsers.getFriendsList());
                mUserDetail.setUserImage(mUsers.getUserImage());
                mUserDetail.setUserName(mUsers.getUserName());
                mUserDetail.setUserEmail(mUsers.getUserEmail());
                mUserDetail.setNoOfFriends(mUsers.getNoOfFriends());
                mUserDetail.setUserId(mUsers.getUserId());
                mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                mUserDetail.setToken(mUsers.getToken());
                mUserDetail.setFriendRequestList(mUsers.getFriendRequestList());

                ArrayList<String> mUserTokenArraylist = new ArrayList<>();
                mUserTokenArraylist.add(mUsers.getToken());

                String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
                PushApis mPushAPIs = new PushApis(mContext);
                mPushAPIs.sendPushNotification(mCurrentUserName + " has rejected a Friend Request ", mUserTokenArraylist, FRIENDS_REJECTED);

                mNotificationMsg = mCurrentUserName + " has rejected a Friend Request";
            }
            addfriendBack(mUserDetail);
        } catch (AGConnectCloudDBException | JSONException e) {
            dismissDialog();
            LogUtil.e("Res error : ", e.getMessage());
        }
    }
}


