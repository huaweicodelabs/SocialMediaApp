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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This POJO class keeps the feeds Info
 */
public class FeedModel {
    private String feedId;
    private String feedDescription;
    private String feedLikes;
    private String feedUnLikes;
    private String uploadedBy;
    private String uploadedDate;
    private String userImage;
    private String mediaId;

    private ArrayList<HashMap<String, String>> arrayList;

    public FeedModel(String mFeedID, String mFeedMediaId, String mFeedDescription, String mFeedLikes, String mFeedUnLikes, String mFeedUploadedBy, String mFeedUploadedDate, String mUserImage, ArrayList<HashMap<String, String>> arrayList) {
        feedId = mFeedID;
        mediaId = mFeedMediaId;
        feedDescription = mFeedDescription;
        feedLikes = mFeedLikes;
        feedUnLikes = mFeedUnLikes;
        uploadedBy = mFeedUploadedBy;
        uploadedDate = mFeedUploadedDate;
        userImage = mUserImage;
        this.arrayList = arrayList;
    }

    public ArrayList<HashMap<String, String>> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<HashMap<String, String>> arrayList) {
        this.arrayList = arrayList;
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

    public String getFeedDescription() {
        return feedDescription;
    }

    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String mUserImage) {
        this.userImage = mUserImage;
    }

    public String getFeedUnLikes() {
        return feedUnLikes;
    }

    public void setFeedUnLikes(String feedUnLikes) {
        this.feedUnLikes = feedUnLikes;
    }
}
