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

import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE_URL;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIATYPE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_TYPE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_URI;
import static com.huawei.hms.socialappsharing.utils.Constants.POST;
import static com.huawei.hms.socialappsharing.utils.Constants.VIDEO;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.PlayActivity;
import com.huawei.hms.socialappsharing.videoplayer.control.HomePageControl;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * This class refers the all the child items loading in one parent item
 */
public class TotalListAdapter extends RecyclerView.Adapter<TotalListAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<Object> imageList;

    // Home page control
    private HomePageControl homePageControl;

    public TotalListAdapter(TotalFeeds context, ArrayList<Object> childItemList) {
        imageList = childItemList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.social_media_app_icon);
        holder.image.setVisibility(View.VISIBLE);
        String uri = (String) ((HashMap) ((ArrayList) imageList.get(position)).get(position)).get(MEDIA_URI);
        String mediaType = (String) ((HashMap) ((ArrayList) imageList.get(position)).get(position)).get(MEDIATYPE);
        Glide.with(mContext).load(imageList.get(position)).load(uri).apply(requestOptions).into(holder.image);
        if (Objects.requireNonNull(mediaType).equals(IMAGE)) {
            holder.video.setVisibility(View.GONE);
        } else {
            holder.video.setVisibility(View.VISIBLE);
        }

        holder.image.setOnClickListener(view -> {
            HiAnalyticsInstance instance = HiAnalytics.getInstance(mContext);
            // Enable tracking of the custom event in proper positions of the code.
            Bundle bundle = new Bundle();
            if (Objects.requireNonNull(mediaType).equals(IMAGE)) {
                Intent intent = new Intent(mContext, PinZoomImage.class);
                intent.putExtra(IMAGE_URL, uri);
                mContext.startActivity(intent);
                bundle.putString(MEDIA_TYPE, IMAGE);
            } else {
                bundle.putString(MEDIA_TYPE, VIDEO);
                try {
                    if (uri != null) {
                        homePageControl = new HomePageControl(mContext);
                        PlayActivity.startPlayActivity(mContext, homePageControl.getInputPlay(uri));
                    }
                } catch (NullPointerException e) {
                    LogUtil.e("Res error : ", e.getMessage());
                }
            }
            instance.onEvent(POST, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView video;

        public ViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.full_child_item);
            this.video = itemView.findViewById(R.id.full_child_item_video);
        }
    }
}

