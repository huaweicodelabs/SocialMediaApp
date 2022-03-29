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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hms.socialappsharing.R;

import java.util.ArrayList;

import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;

/**
 * This adapter class refers the Feed preview Horizantal listview of the files
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final Context mContext;
    ArrayList<String> childItem;
    ArrayList<String> horizontalListFeedType;


    public CustomAdapter(Context context, ArrayList<String> childItem, ArrayList<String> horizontalListFeedType) {
        this.childItem = childItem;
        this.horizontalListFeedType = horizontalListFeedType;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item_horizantal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_foreground);
        if (childItem != null) {
            Glide.with(mContext).load(childItem.get(position)).apply(requestOptions).into(holder.image);
        }
        if (horizontalListFeedType.get(position).equals(IMAGE)) {
            holder.video.setVisibility(View.GONE);
        } else {
            holder.video.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return childItem.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView image;
        AppCompatImageView video;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_child_item);
            video = itemView.findViewById(R.id.video_child_item);
        }
    }
}