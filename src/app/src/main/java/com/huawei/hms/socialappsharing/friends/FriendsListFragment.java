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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.huawei.hms.socialappsharing.main.PageViewModel;
import com.huawei.hms.socialappsharing.models.Users;
import com.huawei.hms.socialappsharing.utils.ObjectTypeInfoHelper;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.DBNAME;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_LIST;
import static com.huawei.hms.socialappsharing.utils.Constants.NAME_VALUE_PAIRS;
import static com.huawei.hms.socialappsharing.utils.Constants.USER_EMAIL;
import static com.huawei.hms.socialappsharing.utils.Constants.VALUES;

/**
 * This class loads the Friends list of the user
 */
@SuppressLint("StaticFieldLeak")
public class FriendsListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZone mCloudDBZone;
    private static String mCurrentUser;
    private static Context mContext;
    private static Activity mActivity;
    private static RecyclerView recyclerView;
    private static SwipeRefreshLayout swipeContainer;
    private static int mUserTotalListFromServer;
    private static ArrayList<UserModel> mUserModelArraylist;
    private static ArrayList<String> mFriendArraylist;

    public static void sendData() {
        if (isNetworkConnected(mContext)) {
            showDialog(mActivity);
            cloudDBZoneCreation();
        } else {
            dismissDialog();
            networkStatusAlert(mContext);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageViewModel pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        mContext = getContext();
        mActivity = getActivity();

        mCurrentUser = PreferenceHandler.getInstance(mContext).getEmailId();

        recyclerView = view.findViewById(R.id.feed_recyclerView);
        LinearLayout createpostlayout = view.findViewById(R.id.create_post_layout);

        createpostlayout.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.feed_recyclerView);

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            if (isNetworkConnected(mContext)) {
                CloudDBZoneQuery<Users> mUserQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
                queryUser(mUserQuery);
            } else {
                swipeContainer.setRefreshing(false);
                networkStatusAlert(mContext);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        return view;
    }

    private void initiateTheCloud() {
        if (isNetworkConnected(mContext)) {
            AGConnectCloudDB.initialize(requireContext());
            mCloudDB = AGConnectCloudDB.getInstance();

            try {
                mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            } catch (AGConnectCloudDBException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
            cloudDBZoneCreation();
        } else {
            dismissDialog();
            networkStatusAlert(mContext);
        }
    }


    private static void cloudDBZoneCreation() {
        CloudDBZoneConfig mConfig = new CloudDBZoneConfig(DBNAME, CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE, CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(cloudDBZone -> {

            mCloudDBZone = cloudDBZone;

            CloudDBZoneQuery<Users> mUserQuery = CloudDBZoneQuery.where(Users.class).equalTo(USER_EMAIL, mCurrentUser);
            queryUser(mUserQuery);

        }).addOnFailureListener(e -> {
            dismissDialog();
            swipeContainer.setRefreshing(false);
        });
    }

    private static void queryUser(CloudDBZoneQuery<Users> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(FriendsListFragment::processQueryResult).addOnFailureListener(e -> {
            dismissDialog();
            swipeContainer.setRefreshing(false);
        });
    }

    private static void processQueryResult(CloudDBZoneSnapshot<Users> snapshot) {
        CloudDBZoneObjectList<Users> userInfoCursor = snapshot.getSnapshotObjects();
        try {
            while (userInfoCursor.hasNext()) {
                Users mUsers = userInfoCursor.next();
                mFriendArraylist = new ArrayList<>();
                String mFriendList = mUsers.getFriendsList();
                if (mFriendList != null) {
                    JSONObject jsonRootObject = new JSONObject(mFriendList);
                    JSONObject jobj = jsonRootObject.getJSONObject(NAME_VALUE_PAIRS);
                    JSONObject jobj1 = jobj.getJSONObject(FRIENDS_LIST);
                    JSONArray jobj2 = jobj1.getJSONArray(VALUES);

                    // Iterate the jsonArray and print the info of JSONObjects
                    for (int i = 0; i < jobj2.length(); i++) {
                        String mFriend = jobj2.getString(i);
                        mFriendArraylist.add(mFriend);
                    }
                }
                queryAllUser();
            }
        } catch (JSONException | AGConnectCloudDBException jsonException) {
            dismissDialog();
            swipeContainer.setRefreshing(false);
            jsonException.printStackTrace();
        }
    }

    private static void queryAllUser() {
        if (mCloudDBZone == null) {
            dismissDialog();

            return;
        }
        Task<CloudDBZoneSnapshot<Users>> queryTask = mCloudDBZone.executeQuery(CloudDBZoneQuery.where(Users.class), CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(snapshot -> {

            mUserModelArraylist = new ArrayList<>();

            mUserTotalListFromServer = snapshot.getSnapshotObjects().size();
            for (int i = 0; i < mUserTotalListFromServer; i++) {
                try {
                    String mUserEmail = snapshot.getSnapshotObjects().get(i).getUserEmail();
                    String mUserName = snapshot.getSnapshotObjects().get(i).getUserName();
                    String mProfileImage = snapshot.getSnapshotObjects().get(i).getUserImage();
                    String mFriendList = snapshot.getSnapshotObjects().get(i).getFriendsList();
                    String mFriendRequest = snapshot.getSnapshotObjects().get(i).getFriendRequestList();
                    String mToken = snapshot.getSnapshotObjects().get(i).getToken();
                    String mUserId = snapshot.getSnapshotObjects().get(i).getUserId();
                    String mNoOfFriends = snapshot.getSnapshotObjects().get(i).getNoOfFriends();
                    String mFriendRequested = snapshot.getSnapshotObjects().get(i).getFriendRequestedList();
                    String mCreatedDate = snapshot.getSnapshotObjects().get(i).getCreatedDate();

                    for (int j = 0; j < mFriendArraylist.size(); j++) {
                        String mFriend = mFriendArraylist.get(j);
                        if (mUserEmail.equals(mFriend)) {
                            if (!mUserId.equals(PreferenceHandler.getInstance(mContext).getUserId())) {
                                mUserModelArraylist.add(new UserModel(mUserEmail, mUserName, mProfileImage, mFriendList, mFriendRequest, mToken, mUserId, mNoOfFriends, mFriendRequested, mCreatedDate));
                            }
                        }
                    }
                } catch (AGConnectCloudDBException e) {
                    dismissDialog();
                    LogUtil.e("Res error : ", e.getMessage());
                }
            }

            dismissDialog();
            swipeContainer.setRefreshing(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            FriendsListAdapter mItemAdapter = new FriendsListAdapter(mContext, mActivity, mUserModelArraylist, mCloudDBZone);

            recyclerView.setAdapter(mItemAdapter);
            recyclerView.setLayoutManager(layoutManager);
        }).addOnFailureListener(e -> {
            dismissDialog();
            swipeContainer.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkConnected(mContext)) {
            showDialog(getActivity());
            if (mCloudDB == null) {
                initiateTheCloud();
            } else {
                cloudDBZoneCreation();
            }
        } else {
            dismissDialog();
            networkStatusAlert(mContext);
        }
    }
}
