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

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.hms.push.SendException;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.TabActivity;
import com.huawei.hms.socialappsharing.utils.PreferenceHandler;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

import static com.huawei.hms.socialappsharing.utils.Constants.MESSAGE_DATA;

/**
 * This class refers Message service of the Push Notification
 */
public class PushService extends HmsMessageService {

    private final static String ACTION = "com.huawei.hms.socialappsharing.PushMessage.action";
    private static final String CHANNEL1 = "Push_Channel_01";
    public static HashSet<String> mMessages = new HashSet<>();

    /**
     * When an app calls the getToken method to apply for a token from the server,
     * if the server does not return the token during current method calling, the server can return the token through this method later.
     * This method callback must be completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * @param token token
     */
    @Override
    public void onNewToken(String token) {
        // send the token to your app server.
        if (!TextUtils.isEmpty(token)) {
            PreferenceHandler.getInstance(getApplicationContext()).setToken(token);
        }
    }

    /**
     * This method is used to receive downstream data messages.
     * This method callback must be completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * @param message RemoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {

        String msgNoteBase = message.getData();
        try {
            showNotification(msgNoteBase);
        } catch (JSONException e) {
            LogUtil.e("Res error : ", e.getMessage());
        }
    }

    @Override
    public void onMessageSent(String msgId) {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra("method", "onMessageSent");
        intent.putExtra("msg", "onMessageSent called, Message id:" + msgId);

        sendBroadcast(intent);
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra("method", "onSendError");
        intent.putExtra("msg", "onSendError called, message id:" + msgId + ", ErrCode:"
                + ((SendException) exception).getErrorCode() + ", description:" + exception.getMessage());
        sendBroadcast(intent);
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
    }

    // Send Notification
    private void showNotification(String msg) throws JSONException {

        boolean isActivityFound = false;
        // check if notifications are on
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

            final String packageName = getApplicationContext().getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    isActivityFound = true;
                    break;
                }
            }
        }

        if (isActivityFound) {
            broadcastDialogIntent(msg);
        } else {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = 1;

            String message;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel mChannel = new NotificationChannel(
                        CHANNEL1, CHANNEL1, importance);
                if (mNotificationManager != null) {
                    mNotificationManager.createNotificationChannel(mChannel);

                    JSONObject mJSONObject = new JSONObject(msg);
                    message = mJSONObject.getString(MESSAGE_DATA);

                    Intent notificationIntent = new Intent(getApplicationContext(), TabActivity.class);
                    Bundle passValue = new Bundle();
                    passValue.putString("msg", msg);
                    notificationIntent.setAction("android.intent.action.MAIN");
                    notificationIntent.addCategory("android.intent.category.LAUNCHER");
                    notificationIntent.putExtras(passValue);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder mBuilder;

                    mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL1).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getResources().getString(R.string.app_name)).setAutoCancel(true).setContentText(message);

                    mBuilder.setContentIntent(intent);
                    mNotificationManager.notify(notificationId, mBuilder.build());
                }

            } else {
                JSONObject mJSONObject = new JSONObject(msg);
                message = mJSONObject.getString(MESSAGE_DATA);

                Intent notificationIntent = new Intent(getApplicationContext(), TabActivity.class);
                Bundle passValue = new Bundle();
                passValue.putString("msg", msg);
                notificationIntent.setAction("android.intent.action.MAIN");
                notificationIntent.addCategory("android.intent.category.LAUNCHER");
                notificationIntent.putExtras(passValue);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                mBuilder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getString(R.string.app_name)).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message).setAutoCancel(true).setSound(soundUri);

                mMessages.add(message);
                NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
                inBoxStyle.setBigContentTitle(getString(R.string.app_name));
                int total = mMessages.size();

                setBadge(this, total);

                for (String mMessage : mMessages) {
                    inBoxStyle.addLine(mMessage);
                }
                for (int i = 0; i < total; i++) {
                    inBoxStyle.addLine(mMessages.toString());
                }

                mBuilder.setContentIntent(intent);
                mBuilder.setStyle(inBoxStyle);
                Notification notification = mBuilder.build();
                mBuilder.setNumber(total);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(notificationId, notification);
            }
        }
    }


    /**
     * Broadcast dialog intent.
     *
     * @param msg the msg
     */
    public void broadcastDialogIntent(String msg) {
        Intent intent = new Intent();
        Bundle passValue = new Bundle();
        passValue.putString("msg", msg);
        intent.putExtras(passValue);
        intent.setAction("com.hms.pushdemo.SHOW_DIALOG");
        sendBroadcast(intent);
    }

    /**
     * Sets badge.
     *
     * @param context the context
     * @param count   the count
     */
    public static void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    /**
     * Gets launcher class name.
     *
     * @param context the context
     * @return the launcher class name
     */
    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }

}