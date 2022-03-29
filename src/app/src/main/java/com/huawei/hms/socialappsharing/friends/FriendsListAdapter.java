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
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.getTimeStamp;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_LIST;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REMOVED;
import static com.huawei.hms.socialappsharing.utils.Constants.NAME_VALUE_PAIRS;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_REMOVED;
import static com.huawei.hms.socialappsharing.utils.Constants.USER_EMAIL;
import static com.huawei.hms.socialappsharing.utils.Constants.VALUES;

/**
 * This class refers the adapter for friends list
 */
public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {

    private final Context mContext;
    private final Activity mActivity;
    private final CloudDBZone mCloudDBZone;
    private final ArrayList<UserModel> mUserModelArraylist;
    private String mCurrentUser;
    private String mFriendUser;

    public FriendsListAdapter(Context mContext, Activity mActivity, ArrayList<UserModel> mUserModelArraylist, CloudDBZone mCloudDBZone) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mUserModelArraylist = mUserModelArraylist;
        this.mCloudDBZone = mCloudDBZone;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel mUserDetails = mUserModelArraylist.get(position);

        mCurrentUser = PreferenceHandler.getInstance(mContext).getEmailId();
        mFriendUser = mUserDetails.getUserEmail();

        String mPhotoURL = PHOTO_MAIN_URL + mUserDetails.getProfileImage();

        Glide.with(mContext).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView);

        holder.textView.setText(mUserDetails.getUserName());

        holder.addFriendBtn.setText(R.string.remove);

        holder.addFriendBtn.setOnClickListener(view -> {
            if (isNetworkConnected(mContext)) {
                showDialog(mActivity);
                CloudDBZoneQuery<Users> query = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
                removeFriendUser(query);

                CloudDBZoneQuery<Users> query1 = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mFriendUser);
                removeFriendUser(query1);
            } else {
                dismissDialog();
                networkStatusAlert(mContext);
            }
        });
    }

    private void removeFriendUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(this::removeRequestQueryResult).addOnFailureListener(e -> dismissDialog());
    }

    private void removeRequestQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        Users mUserDetail = new Users();
        try {
            while (userInfoCursor.hasNext()) {

                Users mUsers = userInfoCursor.next();
                String mUser;
                if (mCurrentUser.equals(mUsers.getUserEmail())) {
                    mUser = mFriendUser;
                } else {
                    ArrayList<String> mUserTokenArraylist = new ArrayList<>();
                    mUserTokenArraylist.add(mUsers.getToken());

                    String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
                    PushApis mPushAPIs = new PushApis(mContext);
                    mPushAPIs.sendPushNotification(mCurrentUserName + " has removed you as a Friend", mUserTokenArraylist, FRIENDS_REMOVED);

                    mUser = mCurrentUser;
                }

                int friendsCount = Integer.parseInt(mUsers.getNoOfFriends());

                String mFriendList = mUsers.getFriendsList();
                if (mFriendList != null) {
                    JSONObject mObj1 = new JSONObject();

                    JSONObject jsonFriendsList = new JSONObject(mFriendList);
                    JSONObject objFriendsList = jsonFriendsList.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject objFriendsList1 = objFriendsList.getJSONObject(FRIENDS_LIST);
                    JSONArray objFriendsList2 = objFriendsList1.getJSONArray(VALUES);

                    // Iterate the jsonArray and print the info of JSONObjects
                    for (int i = 0; i < objFriendsList2.length(); i++) {
                        String mFriend = objFriendsList2.getString(i);
                        if (mFriend.equals(mUser)) {
                            objFriendsList2.remove(i);
                            --friendsCount;
                            break;
                        }
                    }
                    mObj1.put(FRIENDS_LIST, objFriendsList2);

                    Gson gson = new Gson();
                    String mFriendList1 = gson.toJson(mObj1);

                    mUserDetail.setFriendsList(mFriendList1);

                    mUserDetail.setUserName(mUsers.getUserName());
                    mUserDetail.setUserImage(mUsers.getUserImage());
                    mUserDetail.setUserEmail(mUsers.getUserEmail());
                    mUserDetail.setNoOfFriends(String.valueOf(friendsCount));
                    mUserDetail.setUserId(mUsers.getUserId());
                    mUserDetail.setCreatedDate(mUsers.getCreatedDate());
                    mUserDetail.setToken(mUsers.getToken());
                    mUserDetail.setFriendRequestList(mUsers.getFriendRequestList());
                    mUserDetail.setFriendRequestedList(mUsers.getFriendRequestedList());

                    removeFriendBack(mUserDetail);
                }
            }
        } catch (AGConnectCloudDBException | JSONException e) {
            dismissDialog();
            LogUtil.e("Res error : ", e.getMessage());
        }
    }

    private void removeFriendBack(Users users) {
        if (mCloudDBZone == null) {

            return;
        }
        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(users);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            FriendsListFragment.sendData();
            String mCurrentUserName = PreferenceHandler.getInstance(mContext).getDisplayName();
            NotificationList mNotificationList = new NotificationList();
            mNotificationList.setNotificationId(getTimeStamp());
            mNotificationList.setNotificationDate(getTimeStamp());
            mNotificationList.setNotificationMessage(mCurrentUserName + " has removed you as a Friend");
            mNotificationList.setNotificationType(NOTIFICATION_TYPE_FRIEND_REMOVED);
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