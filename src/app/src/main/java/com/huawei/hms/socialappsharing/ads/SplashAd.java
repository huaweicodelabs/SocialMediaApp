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

package com.huawei.hms.socialappsharing.ads;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashView;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.main.FeedFragment;

/**
 * This class refers the Splash Ad loading
 */
public class SplashAd extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);

        // Obtain SplashView.
        SplashView splashView = findViewById(R.id.splash_ad_view);
        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        AdParam adParam = new AdParam.Builder().build();
        // Set a logo image.
        splashView.setLogoResId(R.mipmap.ic_launcher);
        // Set logo description.
        splashView.setMediaNameResId(R.string.app_name);
        // Set the audio focus preemption policy for a video splash ad.
        splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
        // Load the ad. AD_ID indicates the ad slot ID.
        splashView.load(getString(R.string.splash_id), orientation, adParam, splashAdLoadListener);
    }

    SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
        @Override
        public void onAdLoaded() {
            // Called when an ad is loaded successfully.
            FeedFragment.mAdLoad = true;
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Called when an ad failed to be loaded. The app home screen is then displayed.
            Toast.makeText(getApplicationContext(), getString(R.string.ad_failed_load) + " " + errorCode, Toast.LENGTH_SHORT).show();
            FeedFragment.mAdLoad = true;
            finish();
        }

        @Override
        public void onAdDismissed() {
            // Called when the display of an ad is complete. The app home screen is then displayed.
            FeedFragment.mAdLoad = true;
            finish();
        }
    };
}
