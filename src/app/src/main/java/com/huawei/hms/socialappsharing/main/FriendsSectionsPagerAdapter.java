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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.huawei.hms.socialappsharing.friends.FriendsListFragment;
import com.huawei.hms.socialappsharing.friends.FriendsRequestListFragment;
import com.huawei.hms.socialappsharing.friends.FriendsSuggestionsListFragment;

/**
 * This class loads the Friends Section of the user
 */
public class FriendsSectionsPagerAdapter extends FragmentStateAdapter {
    public FriendsSectionsPagerAdapter(@NonNull Context mContext) {
        super((FragmentActivity) mContext);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FriendsListFragment();
            case 1:
                return new FriendsSuggestionsListFragment();
            case 2:
                return new FriendsRequestListFragment();
        }
        return new FeedFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
