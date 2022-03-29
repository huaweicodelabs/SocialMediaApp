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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

/**
 * This class refers the Splash screen activity
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // Enable SDK log recording.
        HiAnalyticsTools.enableLog();
        if (PreferenceHandler.getInstance(getApplicationContext()).getToken().isEmpty()) {
            getToken();
        }
    }

    // Check credentials to jump where after 2 seconds
    private void callHandler() {
        int splashTimeOut = 2000;
        new Handler().postDelayed(() -> {

            if (PreferenceHandler.getInstance(getApplicationContext()).getEmailId().isEmpty()) {
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), TabActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        }, splashTimeOut);
    }

    private class Thread1 implements Runnable {
        public void run() {
            try {
                // Obtain the app ID from the connect-service.json file.
                String appId = BuildConfig.CLIENT_ID;
                // Set tokenScope to HCM.
                String tokenScope = "HCM";
                String token = HmsInstanceId.getInstance(SplashScreen.this).getToken(appId, tokenScope);
                // Check whether the token is empty.
                if (!TextUtils.isEmpty(token)) {
                    sendRegTokenToServer(token);
                }
            } catch (ApiException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
        }
    }

    private void getToken() {
        // Create a thread.
        Thread1 obj = new Thread1();
        Thread t1 = new Thread(obj);
        t1.setName("MyThread");
        t1.setUncaughtExceptionHandler((tr, ex) -> LogUtil.e("Res setUncaughtExceptionHandler -", ex.getMessage()));
        t1.start();
    }

    private void sendRegTokenToServer(String token) {
        PreferenceHandler.getInstance(getApplicationContext()).setToken(token);
    }

    @Override
    protected void onResume() {
        super.onResume();
        callHandler();
    }
}
