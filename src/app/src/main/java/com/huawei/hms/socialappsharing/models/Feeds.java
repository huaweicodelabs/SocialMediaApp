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
 * Definition of ObjectType Feeds.
 *
 * @since 2021-12-09
 */
@PrimaryKeys({"feedId"})
public final class Feeds extends CloudDBZoneObject {
    private String feedId;

    private String feedLikes;

    private String uploadedBy;

    private String uploadedDate;

    private String mediaId;

    private String feedDescription;

    private String userImage;

    private String feedUnlikes;

    public Feeds() {
        super(Feeds.class);
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedLikes(String feedLikes) {
        this.feedLikes = feedLikes;
    }

    public String getFeedLikes() {
        return feedLikes;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedDate(String uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    public String getFeedDescription() {
        return feedDescription;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setFeedUnlikes(String feedUnlikes) {
        this.feedUnlikes = feedUnlikes;
    }

    public String getFeedUnlikes() {
        return feedUnlikes;
    }

}
