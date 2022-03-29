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

import static com.huawei.hms.socialappsharing.TabActivity.actionMessage;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_ACCEPTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REJECTED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REMOVED;
import static com.huawei.hms.socialappsharing.utils.Constants.FRIENDS_REQUESTED;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.huawei.hms.socialappsharing.R;

/**
 * This class loads the Friends list of the user
 */
public class FriendsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        Context mContext = getContext();

        ViewPager2 viewPager = view.findViewById(R.id.friends_viewPager);
        TabLayout tabLayout = view.findViewById(R.id.friends_tabLayout);

        assert mContext != null;
        FriendsSectionsPagerAdapter sectionsPagerAdapter = new FriendsSectionsPagerAdapter(mContext);
        viewPager.setAdapter(sectionsPagerAdapter);

        String[] titles = new String[]{requireContext().getString(R.string.friends), requireContext().getString(R.string.friend_suggestion), requireContext().getString(R.string.friend_request)};

        new TabLayoutMediator(tabLayout, viewPager, ((tab, position) ->
                tab.setText(titles[position]))).attach();
        if(actionMessage != null){
            if(actionMessage.equals(FRIENDS_REQUESTED)){
                TabLayout.Tab tab = tabLayout.getTabAt(2);
                assert tab != null;
                tab.select();
            }else if(actionMessage.equals(FRIENDS_ACCEPTED)){
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                assert tab != null;
                tab.select();
            }else if(actionMessage.equals(FRIENDS_REJECTED)){
                TabLayout.Tab tab = tabLayout.getTabAt(1);
                assert tab != null;
                tab.select();
            }else if(actionMessage.equals(FRIENDS_REMOVED)){
                TabLayout.Tab tab = tabLayout.getTabAt(1);
                assert tab != null;
                tab.select();
            }

            actionMessage = null;
        }


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
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
    public void onDestroyView() {
        super.onDestroyView();
    }
}