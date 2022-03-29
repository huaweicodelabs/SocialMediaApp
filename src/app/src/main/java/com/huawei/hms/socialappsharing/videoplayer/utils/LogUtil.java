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

package com.huawei.hms.socialappsharing.videoplayer.utils;

import com.huawei.hms.common.util.Logger;

/**
 * Log tools
 */
public class LogUtil {
    private static final String TAG = "Res ";

    public static void d(String tag, String msg) {
        Logger.d(getTag(tag), msg);
    }

    public static void d(String msg) {
        Logger.d(getTag(""), msg);
    }

    public static void i(String tag, String msg) {
        Logger.i(getTag(tag), msg);
    }

    public static void i(String msg) {
        Logger.i(getTag(""), msg);
    }

    public static void w(String tag, String msg) {
        Logger.w(getTag(tag), msg);
    }

    public static void e(String tag, String msg) {
        Logger.e(getTag(tag), msg);
    }

    private static String getTag(String tag) {
        if (StringUtil.isEmpty(tag)) {
            return TAG;
        } else {
            return TAG + ":" + tag;
        }
    }
}
