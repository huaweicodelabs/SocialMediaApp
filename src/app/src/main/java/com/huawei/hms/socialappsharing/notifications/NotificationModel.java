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

package com.huawei.hms.socialappsharing.notifications;

/**
 * This POJO class keeps the Notification Info
 */
public class NotificationModel {

    private String NotificationId;
    private String NotificationDate;
    private String NotificationUserId;
    private String NotificationUserImage;
    private String NotificationMessage;
    private String NotificationType;
    private String NotificationTo;

    public NotificationModel(String notificationId, String notificationDate, String notificationUserId, String notificationUserImage, String notificationMessage, String notificationType, String notificationTo) {
        NotificationId = notificationId;
        NotificationDate = notificationDate;
        NotificationUserId = notificationUserId;
        NotificationUserImage = notificationUserImage;
        NotificationMessage = notificationMessage;
        NotificationType = notificationType;
        NotificationTo = notificationTo;
    }


    public void setNotificationId(String NotificationId) {
        this.NotificationId = NotificationId;
    }

    public String getNotificationId() {
        return NotificationId;
    }

    public void setNotificationDate(String NotificationDate) {
        this.NotificationDate = NotificationDate;
    }

    public String getNotificationDate() {
        return NotificationDate;
    }

    public void setNotificationUserId(String NotificationUserId) {
        this.NotificationUserId = NotificationUserId;
    }

    public String getNotificationUserId() {
        return NotificationUserId;
    }

    public void setNotificationUserImage(String NotificationUserImage) {
        this.NotificationUserImage = NotificationUserImage;
    }

    public String getNotificationUserImage() {
        return NotificationUserImage;
    }

    public void setNotificationMessage(String NotificationMessage) {
        this.NotificationMessage = NotificationMessage;
    }

    public String getNotificationMessage() {
        return NotificationMessage;
    }

    public void setNotificationType(String NotificationType) {
        this.NotificationType = NotificationType;
    }

    public String getNotificationType() {
        return NotificationType;
    }

    public void setNotificationTo(String NotificationTo) {
        this.NotificationTo = NotificationTo;
    }

    public String getNotificationTo() {
        return NotificationTo;
    }
}
