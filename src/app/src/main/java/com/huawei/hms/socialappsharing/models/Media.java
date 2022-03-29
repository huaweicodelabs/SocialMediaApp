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

package com.huawei.hms.socialappsharing.models;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType Media.
 *
 * @since 2021-12-09
 */
@PrimaryKeys({"mediaId"})
public final class Media extends CloudDBZoneObject {
    private String mediaId;

    private String mediaCommonId;

    private String mediaType;

    private String mediaURI;

    public Media() {
        super(Media.class);
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaCommonId(String mediaCommonId) {
        this.mediaCommonId = mediaCommonId;
    }

    public String getMediaCommonId() {
        return mediaCommonId;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaURI(String mediaURI) {
        this.mediaURI = mediaURI;
    }

    public String getMediaURI() {
        return mediaURI;
    }

}
