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

package com.huawei.hms.socialappsharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.huawei.agconnect.crash.AGConnectCrash;
import com.huawei.hms.socialappsharing.main.SectionsPagerAdapter;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static com.huawei.hms.socialappsharing.BuildConfig.PHOTO_MAIN_URL;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_ACCEPTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REJECTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REMOVED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REQUESTED;
import static com.huawei.hms.socialappsharing.utils.Constants.MESSAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.MESSAGE_DATA;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_ID;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FEED;
import static com.huawei.hms.socialappsharing.utils.Constants.NOTIFICATION_TYPE_FRIEND_REQUEST;
import static com.huawei.hms.socialappsharing.utils.Constants.POSTED_FEED;
import static com.huawei.hms.socialappsharing.utils.Constants.TITLE;
import static com.huawei.hms.socialappsharing.utils.Constants.TITLE;

/**
 * This class refers the Tab host and switching betweem the activities
 */
public class TabActivity extends AppCompatActivity {

    // Tab Host Titles
    private final int[] tabIcons = new int[]{R.drawable.ic_baseline_home_24, R.drawable.ic_baseline_people_24, R.drawable.ic_baseline_notifications_24};
    private android.app.AlertDialog dialogShow;
    private android.app.AlertDialog.Builder dialog;
    private com.huawei.hms.socialappsharing.databinding.ActivityTabBinding binding;
    public static String actionMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.huawei.hms.socialappsharing.databinding.ActivityTabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new android.app.AlertDialog.Builder(TabActivity.this);
        dialogShow = dialog.create();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this);
        binding.viewPager.setAdapter(sectionsPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, ((tab, position) ->
                tab.setIcon(tabIcons[position]))).attach();

        String mUrl = PreferenceHandler.getInstance(getApplicationContext()).getPhotoURL();

        String mPhotoURL = PHOTO_MAIN_URL + mUrl;
        Glide.with(getApplicationContext()).load(mPhotoURL).circleCrop().placeholder(R.drawable.ic_launcher_foreground).into(binding.profileImage);

        // Register the broadcast receivers dialog
        registerIntents();
        // Receives the new notifications
        onNewIntent(getIntent());

        Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(0)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(1)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(2)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray), PorterDuff.Mode.SRC_IN);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(0)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else if (tab.getPosition() == 1) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(1)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                } else if (tab.getPosition() == 2) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(2)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(0)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray), PorterDuff.Mode.SRC_IN);
                } else if (tab.getPosition() == 1) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(1)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray), PorterDuff.Mode.SRC_IN);
                } else if (tab.getPosition() == 2) {
                    Objects.requireNonNull(Objects.requireNonNull(binding.tabLayout.getTabAt(2)).getIcon()).setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // // Register the broadcast receivers dialog
    private void registerIntents() {
        // Notification
        IntentFilter filter = new IntentFilter(Intent.ACTION_DEFAULT);
        filter.addAction("com.hms.pushdemo.SHOW_DIALOG");
        filter.setPriority(0);
        this.registerReceiver(this.showNotificationDialogReceiver, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String mMessage = bundle.getString("msg");
            JSONObject mJSONObject;
            try {
                if(mMessage!=null){
                    mJSONObject = new JSONObject(mMessage);
                    actionMessage = mJSONObject.getString(MESSAGE);

                    if (actionMessage.equals(FRIENDS_REQUESTED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                        assert tab != null;
                        tab.select();
                    } else if (actionMessage.equals(FRIENDS_ACCEPTED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                        assert tab != null;
                        tab.select();
                    } else if (actionMessage.equals(FRIENDS_REJECTED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                        assert tab != null;
                        tab.select();
                    } else if (actionMessage.equals(FRIENDS_REMOVED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                        assert tab != null;
                        tab.select();
                    } else if (actionMessage.equals(POSTED_FEED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(0);
                        assert tab != null;
                        tab.select();
                        actionMessage = null;
                    }
                }else{
                   mMessage = bundle.getString(NOTIFICATION_ID);

                    if (mMessage.equals(NOTIFICATION_TYPE_FEED)) {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(0);
                        assert tab != null;
                        tab.select();
                    } else {
                        TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                        assert tab != null;
                        tab.select();
                    }
                }

            } catch (NullPointerException|JSONException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
        }
    }


    // Mention that After getting the notification showing in alert dialog
    private final BroadcastReceiver showNotificationDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String mMessage = bundle.getString("msg");
                JSONObject mJSONObject;
                try {
                    mJSONObject = new JSONObject(mMessage);
                    String mTitle = mJSONObject.getString(TITLE);
                    String message = mJSONObject.getString(MESSAGE_DATA);
                    actionMessage = mJSONObject.getString(MESSAGE);

                    if (Objects.requireNonNull(action).equals("com.hms.pushdemo.SHOW_DIALOG")) {
                        if (!dialogShow.isShowing()) {
                            dialog.setTitle(mTitle);
                            dialog.setMessage(message);
                            dialog.setPositiveButton(getResources().getString(R.string.show_me), (dialog, which) -> {
                                // TODO Auto-generated method stub
                                dialog.dismiss();

                                if (actionMessage.equals(FRIENDS_REQUESTED)) {
                                    TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                                    assert tab != null;
                                    tab.select();
                                } else if (actionMessage.equals(FRIENDS_ACCEPTED)) {
                                    TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                                    assert tab != null;
                                    tab.select();
                                } else if (actionMessage.equals(FRIENDS_REJECTED)) {
                                    TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                                    assert tab != null;
                                    tab.select();
                                } else if (actionMessage.equals(FRIENDS_REMOVED)) {
                                    TabLayout.Tab tab = binding.tabLayout.getTabAt(1);
                                    assert tab != null;
                                    tab.select();
                                } else if (actionMessage.equals(POSTED_FEED)) {
                                    TabLayout.Tab tab = binding.tabLayout.getTabAt(0);
                                    assert tab != null;
                                    tab.select();
                                    actionMessage = null;
                                }
                            });
                            dialog.setNegativeButton(getResources().getString(R.string.okTxt), (dialog, which) -> {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            });
                            dialogShow = dialog.create();
                            dialogShow.setCancelable(false);
                            dialogShow.show();
                        }
                    }
                } catch (JSONException e) {
                    LogUtil.e("Res error : ", e.getMessage());
                }
            }
        }
    };
}
