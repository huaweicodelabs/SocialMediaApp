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
 * Definition of ObjectType Users.
 *
 * @since 2021-12-09
 */
@PrimaryKeys({"userId", "userEmail"})
public final class Users extends CloudDBZoneObject {
    private String userId;

    private String userName;

    private String userEmail;

    private String userImage;

    private String createdDate;

    private String token;

    private String noOfFriends;

    private String friendsList;

    private String friendRequestList;

    private String friendRequestedList;

    public Users() {
        super(Users.class);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setNoOfFriends(String noOfFriends) {
        this.noOfFriends = noOfFriends;
    }

    public String getNoOfFriends() {
        return noOfFriends;
    }

    public void setFriendsList(String friendsList) {
        this.friendsList = friendsList;
    }

    public String getFriendsList() {
        return friendsList;
    }

    public void setFriendRequestList(String friendRequestList) {
        this.friendRequestList = friendRequestList;
    }

    public String getFriendRequestList() {
        return friendRequestList;
    }

    public void setFriendRequestedList(String friendRequestedList) {
        this.friendRequestedList = friendRequestedList;
    }

    public String getFriendRequestedList() {
        return friendRequestedList;
    }

}
