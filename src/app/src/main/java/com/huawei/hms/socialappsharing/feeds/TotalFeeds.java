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

import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE_URL;
import static com.huawei.hms.socialappsharing.utils.Constants.SIZE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.util.ArrayList;

/**
 * This class refers the all the child items loading in one parent item
 */
public class TotalFeeds extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_feeds);

        RecyclerView recyclerViewItem = findViewById(R.id.full_recyclerview);
        ImageView backBtn = findViewById(R.id.back_btn);

        ArrayList<Object> childItemList = new ArrayList<>();
        
        Intent intent = getIntent();
        Object obj = intent.getSerializableExtra(IMAGE_URL);
        Integer s = (Integer) intent.getSerializableExtra(SIZE);

        try{
            for (int i=0;i<s;i++){
                childItemList.add(obj);
            }
        }catch (NullPointerException e){
            LogUtil.d("Res TotalFeeds :" + e.getMessage());
        }

        // Initialise the Linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(TotalFeeds.this);
        TotalListAdapter parentItemAdapter = new TotalListAdapter(TotalFeeds.this, childItemList);

        recyclerViewItem.setAdapter(parentItemAdapter);
        recyclerViewItem.setLayoutManager(layoutManager);

        backBtn.setOnClickListener(view -> finish());
    }
}