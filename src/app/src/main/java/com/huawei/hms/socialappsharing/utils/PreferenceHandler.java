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

package com.huawei.hms.socialappsharing.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * This class refers the Shared preference store
 */
public class PreferenceHandler {

    private static SharedPreferences sh;

    private PreferenceHandler(Context mContext) {
        sh = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    private static PreferenceHandler instance = null;


    public synchronized static PreferenceHandler getInstance(Context mContext) {
        if (instance == null) {
            instance = new PreferenceHandler(mContext);

            try {
                SharedPreferences spreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor spreferencesEditor = spreferences.edit();
                spreferencesEditor.clear();
                spreferencesEditor.apply();
            } catch (RuntimeException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }

            try {
                // this is equivalent to using deprecated MasterKeys.AES256_GCM_SPEC
                MasterKey masterKey = new MasterKey.Builder(mContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();

                sh = EncryptedSharedPreferences.create(
                        mContext,
                        "secret_shared_prefs_file",
                        masterKey, // masterKey created above
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            } catch (GeneralSecurityException | IllegalArgumentException | IOException e) {
                LogUtil.e("Res error : ", e.getMessage());
            }
        }
        return instance;
    }


    public int getImageName() {
        return sh.getInt("imagename", 0);
    }


    public void setImageName(int imagename) {
        sh.edit().putInt("imagename", imagename).apply();
    }

    public String getEmailId() {
        return sh.getString("emailId", "");
    }

    public void setEmailId(String emailId) {
        sh.edit().putString("emailId", emailId).apply();
    }

    public void setDisplayName(String mDisplayName) {
        sh.edit().putString("DisplayName", mDisplayName).apply();
    }

    public String getDisplayName() {
        return sh.getString("DisplayName", "");
    }

    public void setPhotoURL(String mPhotoURL) {
        sh.edit().putString("PhotoURL", mPhotoURL).apply();
    }

    public String getPhotoURL() {
        return sh.getString("PhotoURL", "");
    }

    public String getUserId() {
        return sh.getString("UserId", "");
    }

    public void setUserId(String UserId) {
        sh.edit().putString("UserId", UserId).apply();
    }

    public void setToken(String Token) {
        sh.edit().putString("Token", Token).apply();
    }

    public String getToken() {
        return sh.getString("Token", "");
    }

    public void setFeedAddress(String mFeedAddress) {
        sh.edit().putString("FeedAddress", mFeedAddress).apply();
    }

    public String getFeedAddress() {
        return sh.getString("FeedAddress", "");
    }

    public void setFeedDescription(String mDescription) {
        sh.edit().putString("FeedDescription", mDescription).apply();
    }

    public String getFeedDescription() {
        return sh.getString("FeedDescription", "");
    }

    public void setFeedTaggedFriends(String mFeedTaggedFriends) {
        sh.edit().putString("FeedTaggedFriends", mFeedTaggedFriends).apply();
    }

    public String getFeedTaggedFriends() {
        return sh.getString("FeedTaggedFriends", "");
    }

    public void setFeedMapLatitude(String mFeedMapLatitude) {
        sh.edit().putString("FeedMapLatitude", mFeedMapLatitude).apply();
    }

    public String getFeedMapLatitude() {
        return sh.getString("FeedMapLatitude", "");
    }

    public void setFeedMapLongtitude(String mFeedMapLongtitude) {
        sh.edit().putString("FeedMapLongtitude", mFeedMapLongtitude).apply();
    }

    public String getFeedMapLongtitude() {
        return sh.getString("FeedMapLongtitude", "");
    }

}
