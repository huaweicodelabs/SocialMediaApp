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

package com.huawei.hms.socialappsharing.friends;

/**
 * This POJO class keeps the User Info
 */
public class UserModel {

    private String mUserEmail;
    private String mUserName;
    private String mProfileImage;
    private String mFriendList;
    private String mFriendRequest;
    private String mToken;
    private String mUserId;
    private String mNoOfFriends;
    private String mFriendRequested;
    private String mCreatedDate;

    public UserModel(String mUserEmail, String mUserName, String mProfileImage, String mFriendList, String mFriendRequest, String mToken, String mUserId, String mNoOfFriends,String mFriendRequested, String mCreatedDate) {
        this.mUserEmail = mUserEmail;
        this.mUserName = mUserName;
        this.mProfileImage = mProfileImage;
        this.mFriendList = mFriendList;
        this.mFriendRequest = mFriendRequest;
        this.mToken = mToken;
        this.mUserId = mUserId;
        this.mNoOfFriends = mNoOfFriends;
        this.mFriendRequested = mFriendRequested;
        this.mCreatedDate = mCreatedDate;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String mProfileImage) {
        this.mProfileImage = mProfileImage;
    }

    public String getFriendList() {
        return mFriendList;
    }

    public void setFriendList(String mFriendList) {
        this.mFriendList = mFriendList;
    }

    public String getFriendRequest() {
        return mFriendRequest;
    }

    public void setFriendRequest(String mFriendRequest) {
        this.mFriendRequest = mFriendRequest;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getNoOfFriends() {
        return mNoOfFriends;
    }

    public void setNoOfFriends(String mNoOfFriends) {
        this.mNoOfFriends = mNoOfFriends;
    }


    public String getFriendRequested() {
        return mFriendRequested;
    }

    public void setFriendRequested(String mFriendRequested) {
        this.mFriendRequested = mFriendRequested;
    }

    public String getmCreatedDate() {
        return mCreatedDate;
    }

    public void setmCreatedDate(String mCreatedDate) {
        this.mCreatedDate = mCreatedDate;
    }
}
