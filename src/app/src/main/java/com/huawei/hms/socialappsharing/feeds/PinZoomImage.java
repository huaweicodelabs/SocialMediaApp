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

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.huawei.hms.socialappsharing.ads.SplashAd;
import com.huawei.hms.socialappsharing.R;

/**
 * This class refers the pin zoom function
 */
public class PinZoomImage extends AppCompatActivity {

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 0.1f;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_zoom);

        Intent intent1 = new Intent(getApplicationContext(), SplashAd.class);
        startActivity(intent1);

        // initialize the view and the gesture detector
        mImageView = findViewById(R.id.imageView);
        ImageView backBtn = findViewById(R.id.back_btn);

        Intent intent = getIntent();
        String mURL = intent.getStringExtra(IMAGE_URL);

        Glide.with(getApplicationContext()).load(mURL).placeholder(R.drawable.ic_launcher_foreground).into(mImageView);

        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        backBtn.setOnClickListener(view -> finish());
    }

    // this redirects all touch events in the activity to the gesture detector
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // when a scale gesture is detected, use it to resize the image
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.75f, Math.min(mScaleFactor, 5.0f));
            mImageView.setScaleX(mScaleFactor);
            mImageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}