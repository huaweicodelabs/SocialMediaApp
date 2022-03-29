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

import android.content.Context;

import android.os.StrictMode;

import com.google.gson.Gson;
import com.huawei.hms.socialappsharing.BuildConfig;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class refers API of the Push Notification to send
 */
public class PushApis {

    private final Context context;

    public PushApis(Context context) {
        this.context = context;
    }

    public void sendPushNotification(String messageData, ArrayList<String> userPushTokens, String action) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            StringBuilder response = new StringBuilder();
            URL url = new URL(BuildConfig.TOKEN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("POST", "/oauth2/v3/token   HTTP/1.1");
            connection.setRequestProperty("Host", "oauth-login.cloud.huawei.com");
            HashMap<String, String> params = new HashMap<>();
            params.put("grant_type", "client_credentials");
            params.put("client_secret", BuildConfig.CLIENT_SECRET);
            params.put("client_id", BuildConfig.CLIENT_ID);
            String postDataLength = getDataString(params);
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(postDataLength);

            writer.flush();
            writer.close();
            os.close();
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                JSONObject result = new JSONObject(response.toString());
                if(result.has("access_token")){
                    String access_token = result.getString("access_token");
                    triggerPush(access_token, messageData, userPushTokens,action);
                }else {
                    LogUtil.e("Res error :  ","Access Token expired");
                }
            } else {
                LogUtil.e("Res error :  ","Access Token expired");
            }
        } catch (Exception e) {
                LogUtil.e("Res error : " , e.getMessage());
        }
    }

    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first){
                first = false;
            }else{
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private void triggerPush(String bearer, String messageData, ArrayList<String> userPushTokens, String action) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            StringBuilder response = new StringBuilder();
            URL url = new URL("https://push-api.cloud.huawei.com/v1/" + BuildConfig.CLIENT_ID + "/messages:send");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "Bearer " + bearer);
            connection.setRequestProperty("Host", "oauth-login.cloud.huawei.com");
            connection.setRequestProperty("POST", "/oauth2/v2/token   HTTP/1.1");
            OutputStream os = connection.getOutputStream();

            String mPhotoURL = PreferenceHandler.getInstance(context).getPhotoURL();

            Data data = new Data();
            data.message = action;
            data.sender_image = mPhotoURL;
            data.messageData = messageData;
            data.title = context.getResources().getString(R.string.app_name);

            ArrayList<String> token = new ArrayList<>(userPushTokens);

            Message message = new Message();
            message.tokens = token;
            message.data = data.toString();

            PushMessageRequest pushMessageRequest = new PushMessageRequest();
            pushMessageRequest.message = message;
            pushMessageRequest.validate_only = false;

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));

            Gson gson = new Gson();
            JSONObject jsonObject = new JSONObject(gson.toJson(pushMessageRequest, PushMessageRequest.class));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
            os.close();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
