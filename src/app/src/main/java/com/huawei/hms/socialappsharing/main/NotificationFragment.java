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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.huawei.hms.socialappsharing.models.NotificationList;
import com.huawei.hms.socialappsharing.notifications.NotificationModel;
import com.huawei.hms.socialappsharing.notifications.NotificationsListAdapter;
import com.huawei.hms.socialappsharing.utils.ObjectTypeInfoHelper;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.util.ArrayList;

import static com.huawei.hms.socialappsharing.utils.CommonMember.dismissDialog;
import static com.huawei.hms.socialappsharing.utils.CommonMember.isNetworkConnected;
import static com.huawei.hms.socialappsharing.utils.CommonMember.networkStatusAlert;
import static com.huawei.hms.socialappsharing.utils.CommonMember.showDialog;
import static com.huawei.hms.socialappsharing.utils.Constants.DBNAME;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_DATE;

/**
 * This class loads the Notifications Section of the user
 */
@SuppressLint("StaticFieldLeak")
public class NotificationFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZone mCloudDBZone;
    private static Context mContext;
    private static FragmentActivity mActivity;
    private static RecyclerView recyclerView;
    private static SwipeRefreshLayout swipeContainer;
    private static final ArrayList<NotificationModel> NOTIFICATION_ARRAYLIST = new ArrayList<>();

    public static void refreshNotifications() {
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        mContext = getContext();
        mActivity = getActivity();

        recyclerView = view.findViewById(R.id.feed_recyclerView);
        LinearLayout createpostlayout = view.findViewById(R.id.create_post_layout);

        createpostlayout.setVisibility(View.GONE);

        recyclerView = view.findViewById(R.id.feed_recyclerView);

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            if (isNetworkConnected(mContext)) {
                CloudDBZoneQuery<NotificationList> mUserQuery = CloudDBZoneQuery.where(NotificationList.class).orderByDesc(NOTIFICATION_DATE);
                queryNotificationList(mUserQuery);
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
            CloudDBZoneQuery<NotificationList> mUserQuery = CloudDBZoneQuery.where(NotificationList.class).orderByDesc(NOTIFICATION_DATE);
            queryNotificationList(mUserQuery);
        }).addOnFailureListener(e -> {
            dismissDialog();
            swipeContainer.setRefreshing(false);
        });
    }

    private static void queryNotificationList(CloudDBZoneQuery<NotificationList> query) {
        if (mCloudDBZone == null) {
            dismissDialog();
            return;
        }
        Task<CloudDBZoneSnapshot<NotificationList>> queryTask = mCloudDBZone.executeQuery(query, CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(NotificationFragment::processQueryResult);
        queryTask.addOnFailureListener(e -> {
            dismissDialog();
            swipeContainer.setRefreshing(false);
        });
    }

    private static void processQueryResult(CloudDBZoneSnapshot<NotificationList> snapshot) {
        NOTIFICATION_ARRAYLIST.clear();
        CloudDBZoneObjectList<NotificationList> notificationInfoCursor = snapshot.getSnapshotObjects();
        try {
            while (notificationInfoCursor.hasNext()) {
                NotificationList mNotificationList = notificationInfoCursor.next();
                String notificationId = mNotificationList.getNotificationId();
                String notificationDate = mNotificationList.getNotificationDate();
                String notificationUserId = mNotificationList.getNotificationUserId();
                String notificationUserImage = mNotificationList.getNotificationUserImage();
                String notificationMessage = mNotificationList.getNotificationMessage();
                String notificationType = mNotificationList.getNotificationType();
                String notificationTo = mNotificationList.getNotificationTo();

                NOTIFICATION_ARRAYLIST.add(new NotificationModel(notificationId, notificationDate, notificationUserId, notificationUserImage, notificationMessage, notificationType, notificationTo));
            }
            dismissDialog();
            swipeContainer.setRefreshing(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            NotificationsListAdapter mItemAdapter = new NotificationsListAdapter(mContext, mActivity, NOTIFICATION_ARRAYLIST, mCloudDBZone);

            recyclerView.setAdapter(mItemAdapter);
            recyclerView.setLayoutManager(layoutManager);
        } catch (AGConnectCloudDBException jsonException) {
            dismissDialog();
            swipeContainer.setRefreshing(false);
            jsonException.printStackTrace();
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            e.printStackTrace();
        }
    }
}
