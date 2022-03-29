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

import android.annotation.SuppressLint;
import android.app.Application;

import com.huawei.hms.socialappsharing.videoplayer.utils.Constants;
import com.huawei.hms.videokit.player.InitFactoryCallback;
import com.huawei.hms.videokit.player.LogConfigInfo;
import com.huawei.hms.videokit.player.WisePlayerFactory;
import com.huawei.hms.videokit.player.WisePlayerFactoryOptionsExt;

/**
 * Application
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static WisePlayerFactory wisePlayerFactory = null;

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
    }

    /**
     * Init the player
     */
    private void initPlayer() {
        // DeviceId test is used in the demo, specific access to incoming deviceId after encryption
        WisePlayerFactoryOptionsExt.Builder factoryOptions =
            new WisePlayerFactoryOptionsExt.Builder().setDeviceId("xxx");
        LogConfigInfo logCfgInfo =
            new LogConfigInfo(Constants.LEVEL_DEBUG, "", Constants.LOG_FILE_NUM, Constants.LOG_FILE_SIZE);
        factoryOptions.setLogConfigInfo(logCfgInfo);
        WisePlayerFactory.initFactory(this, factoryOptions.build(), INIT_FACTORY_CALLBACK);
    }

    /**
     * Player initialization callback
     */
    private static final InitFactoryCallback INIT_FACTORY_CALLBACK = new InitFactoryCallback() {
        @Override
        public void onSuccess(WisePlayerFactory wisePlayerFactory) {
            setWisePlayerFactory(wisePlayerFactory);
        }

        @Override
        public void onFailure(int errorCode, String reason) {
        }
    };

    /**
     * Get WisePlayer Factory
     * 
     * @return WisePlayer Factory
     */
    public static WisePlayerFactory getWisePlayerFactory() {
        return wisePlayerFactory;
    }

    private static void setWisePlayerFactory(WisePlayerFactory wisePlayerFactory) {
        MyApplication.wisePlayerFactory = wisePlayerFactory;
    }

}
