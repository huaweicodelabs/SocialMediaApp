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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hms.socialappsharing.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE_URL;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIATYPE;
import static com.huawei.hms.socialappsharing.utils.Constants.MEDIA_URI;
import static com.huawei.hms.socialappsharing.utils.Constants.SIZE;

/**
 * This class refers the Feed nested list child items
 */
public class FeedsChildAdapter extends RecyclerView.Adapter<FeedsChildAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<HashMap<String, String>> childItem;

    public FeedsChildAdapter(Context context, ArrayList<HashMap<String, String>> childItem) {
        this.childItem =childItem;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_foreground);

        if(Objects.requireNonNull(childItem.get(position).get(MEDIATYPE)).equals(IMAGE)){
            holder.video.setVisibility(View.GONE);
        }else{
            holder.video.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext).load(childItem.get(position).get(MEDIA_URI)).apply(requestOptions).into(holder.image);

        holder.image.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, TotalFeeds.class);
            intent.putExtra(IMAGE_URL,childItem);
            intent.putExtra(SIZE,childItem.size());
            mContext.startActivity(intent);
        });

        // Displaying item count
        String count = String.valueOf(childItem.size() - 3);
        if (position == 3) {
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText("+"+count);
        }else{
            holder.count.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        int limit = 4;
        return Math.min(childItem.size(), limit);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView ChildItemTitle;
        ImageView image;
        ImageView video;
        TextView count;

        ViewHolder(View itemView) {
            super(itemView);
            ChildItemTitle = itemView.findViewById(R.id.child_item_title);
            image = itemView.findViewById(R.id.img_child_item);
            video = itemView.findViewById(R.id.video_child_item);
            count = itemView.findViewById(R.id.count);
        }
    }
}

