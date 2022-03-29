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

package com.huawei.hms.socialappsharing.videoplayer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.contract.OnDialogInputValueListener;
import com.huawei.hms.socialappsharing.videoplayer.contract.OnPlaySettingListener;
import com.huawei.hms.socialappsharing.videoplayer.view.PlaySettingDialog;

import java.util.List;

/**
 * Dialog tools
 */
public class DialogUtil {
    /**
     * Play activity Settings dialog
     *
     * @param context               Context
     * @param settingType           Set the play type
     * @param showTextList          Set the list of options
     * @param selectIndex           The default Settings index
     * @param onPlaySettingListener Click listener
     */
    public static void onSettingDialogSelectIndex(Context context, int settingType, List<String> showTextList, int selectIndex, OnPlaySettingListener onPlaySettingListener) {
        PlaySettingDialog dialog = new PlaySettingDialog(context).setList(showTextList);
        dialog.setTitle(StringUtil.getStringFromResId(context, R.string.settings));
        dialog.setSelectIndex(selectIndex);
        dialog.setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        dialog.initDialog(onPlaySettingListener, settingType);
        dialog.show();
    }

    /**
     * Play activity Gettings dialog
     *
     * @param context               Context
     * @param gettingType           Get the play type
     * @param showTextList          Get the list of options
     * @param selectIndex           The default Gettings index
     * @param onPlaySettingListener Click listener
     */
    public static void onGettingDialogSelectIndex(Context context, int gettingType, List<String> showTextList, int selectIndex, OnPlaySettingListener onPlaySettingListener) {
        PlaySettingDialog dialog = new PlaySettingDialog(context).setList(showTextList);
        dialog.setTitle(StringUtil.getStringFromResId(context, R.string.gettings));
        dialog.setSelectIndex(selectIndex);
        dialog.setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        dialog.initDialog(onPlaySettingListener, gettingType);
        dialog.show();
    }

    /**
     * Play activity Settings dialog
     *
     * @param context               Context
     * @param settingType           Set the play type
     * @param showTextList          Set the list of options
     * @param selectValue           The default Settings string
     * @param onPlaySettingListener Click listener
     */
    public static void onSettingDialogSelectValue(Context context, int settingType, List<String> showTextList, String selectValue, OnPlaySettingListener onPlaySettingListener) {
        PlaySettingDialog dialog = new PlaySettingDialog(context).setList(showTextList);
        dialog.setTitle(StringUtil.getStringFromResId(context, R.string.settings));
        dialog.setSelectValue(selectValue).setNegativeButton(StringUtil.getStringFromResId(context, R.string.setting_cancel), null);
        dialog.initDialog(onPlaySettingListener, settingType);
        dialog.show();
    }

    /**
     * Get the volume Settings dialog
     *
     * @param context                    Context
     * @param onDialogInputValueListener Click listener
     */
    public static void showSetVolumeDialog(Context context, final OnDialogInputValueListener onDialogInputValueListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.set_volume_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(StringUtil.getStringFromResId(context, R.string.video_set_volume)).setView(view).create();
        dialog.show();
        final EditText volumeValueEt = view.findViewById(R.id.set_volume_et);
        Button okBt = view.findViewById(R.id.set_volume_bt_ok);
        Button cancelBt = view.findViewById(R.id.set_volume_bt_cancel);
        okBt.setOnClickListener(v -> {
            if (onDialogInputValueListener != null) {
                String inputText = "";
                if (volumeValueEt.getText() != null) {
                    inputText = volumeValueEt.getText().toString();
                }
                onDialogInputValueListener.dialogInputListener(inputText);
                dialog.dismiss();
            }
        });
        cancelBt.setOnClickListener(v -> dialog.dismiss());
    }
}
