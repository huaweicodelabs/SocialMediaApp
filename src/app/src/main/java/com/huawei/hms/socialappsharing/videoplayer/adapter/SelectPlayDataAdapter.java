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

package com.huawei.hms.socialappsharing.videoplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.contract.OnItemClickListener;
import com.huawei.hms.socialappsharing.videoplayer.entity.PlayEntity;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Play recyclerView adapter
 */
public class SelectPlayDataAdapter extends RecyclerView.Adapter<SelectPlayDataAdapter.PlayViewHolder> {
    private static final String TAG = "SelectPlayDataAdapter";

    // Data sources list
    private final List<PlayEntity> playList;

    // Context
    private final Context context;

    // Click item listener
    private final OnItemClickListener onItemClickListener;

    /**
     * Constructor
     *
     * @param context             Context
     * @param onItemClickListener Listener
     */
    public SelectPlayDataAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        playList = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Set list data
     *
     * @param playList Play data
     */
    public void setSelectPlayList(List<PlayEntity> playList) {
        if (this.playList.size() > 0) {
            this.playList.clear();
        }
        this.playList.addAll(playList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.select_play_item, parent, false);
        return new PlayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (playList.size() > position) {
            PlayEntity playEntity = playList.get(position);
            if (playEntity == null) {
                LogUtil.i(TAG, "current item data is empty.");
                return;
            }
            StringUtil.setTextValue(holder.playName, playEntity.getName());
            StringUtil.setTextValue(holder.playUrl, playEntity.getUrl());
            StringUtil.setTextValue(holder.playType, String.valueOf(playEntity.getUrlType()));
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    /**
     * Show view holder
     */
    static class PlayViewHolder extends ViewHolder {

        // The video name
        private final TextView playName;

        // The video type
        private final TextView playType;

        // The video url
        private final TextView playUrl;

        /**
         * Constructor
         *
         * @param itemView Item view
         */
        public PlayViewHolder(View itemView) {
            super(itemView);
            playName = itemView.findViewById(R.id.play_name);
            playType = itemView.findViewById(R.id.play_type);
            playUrl = itemView.findViewById(R.id.play_url);
        }
    }
}
