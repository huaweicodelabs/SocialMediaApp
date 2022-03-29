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

package com.huawei.hms.socialappsharing.pushmessage;

import static com.huawei.hms.socialappsharing.utils.Constants.MESSAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.MESSAGE_DATA;
import static com.huawei.hms.socialappsharing.utils.Constants.SENDER_IMAGE;
import static com.huawei.hms.socialappsharing.utils.Constants.TITLE;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * This model class refers data of the Push Notification
 */
public class Data {

    @SerializedName(TITLE)
    public String title;

    @SerializedName(MESSAGE)
    public String message;

    @SerializedName(SENDER_IMAGE)
    public String sender_image;

    @SerializedName(MESSAGE_DATA)
    public String messageData;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" +
                "'" + TITLE + "'" + "=" + "'" + title + "'" + "," +
                "'" + MESSAGE + "'" + "=" + "'" + message + "'" + "," +
                "'" + MESSAGE_DATA + "'" + "=" + "'" + messageData + "'" + "," +
                "'" + SENDER_IMAGE + "'" + "=" + "'" + sender_image + "'" + "}";

    }


}