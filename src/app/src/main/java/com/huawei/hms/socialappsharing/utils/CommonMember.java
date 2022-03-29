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

import static com.huawei.hms.socialappsharing.utils.Constants.IMAGE;

import static com.huawei.hms.socialappsharing.utils.Constants.VIDEO;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * This class refers the Common member function
 */
public class CommonMember {
    private static ProgressDialog pd;

    public static void showDialog(Activity getActivity) {
        dismissDialog();
        pd = new ProgressDialog(getActivity);
        pd.setMessage(getActivity.getString(R.string.please_wait));
        pd.show();
    }

    /**
     * dismiss the Dialog
     */
    public static void dismissDialog() {
        if (pd != null) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
    }


    /**
     * Check whether network is available or not
     *
     * @param getActivity Activity
     * @return isConnected
     */
    public static boolean isNetworkConnected(Context getActivity) {
        ConnectivityManager cm = (ConnectivityManager) getActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    /**
     * Show alert for network Status
     *
     * @param getActivity Activity
     */
    public static void networkStatusAlert(Context getActivity) {
        try {
            String mConfirmationText;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity);
            builder.setMessage(getActivity.getResources().getString(R.string.no_internet_access));
            builder.setTitle(getActivity.getResources().getString(R.string.NetworkStatusTxt));
            mConfirmationText = getActivity.getResources().getString(R.string.okTxt);
            builder.setNeutralButton(mConfirmationText, (dialog, which) -> {
                // TODO Auto-generated method stub
                dialog.dismiss();
            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            LogUtil.e("Res error : ", e.getMessage());
        }
    }


    /**
     * Alert dialog for shows the error message
     *
     * @param message message
     * @param context context
     * @return dialog
     */
    public static AlertDialog getErrorDialog(String message, Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(context.getString(R.string.app_name));
        dialog.setMessage(message);
        dialog.setPositiveButton(context.getResources().getString(R.string.okTxt), (dialog1, which) -> {
            dialog1.dismiss();
        });

        AlertDialog errorDialog = dialog.create();
        errorDialog.setCanceledOnTouchOutside(true);
        return errorDialog;
    }

    /**
     * Get the file path
     *
     * @param context - context
     * @param uri     - uri
     * @return the file path
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if (IMAGE.equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if (VIDEO.equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for the Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnindex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnindex);
            }
        } catch (IllegalStateException e) {
            LogUtil.e("Res error : ", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getTimeStamp() {
        long tsLong = System.currentTimeMillis() / 1000;
        return Long.toString(tsLong);
    }

    public static String getDate(String time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(time) * 1000L);

        return DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
    }
}
