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

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * This model class refers message structure of the Push Notification
 */
public class Message {

    @SerializedName("token")
    public ArrayList<String> tokens = new ArrayList<>();

    @SerializedName("data")
    public String data;
}