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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class refers the Dialog selections
 */
public class SelectDialog implements OnClickListener {
    AlertDialog.Builder builder;

    Handler handler;

    int messageId;

    List<Pair<String, String>> list = new ArrayList<>();

    int seletedIndex;

    public SelectDialog(Context context) {
        builder = new AlertDialog.Builder(context);
    }

    public SelectDialog setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }

    public SelectDialog setList(List<String> strList) {
        list = new ArrayList<>();
        if (strList != null) {
            for (String temp : strList) {
                list.add(new Pair<>(temp, temp));
            }
        }
        return this;
    }

    public void setHandler(Handler handler, int messageId) {
        this.handler = handler;
        this.messageId = messageId;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Message message = handler.obtainMessage(messageId, which, 0, list.get(which).first);
        handler.sendMessage(message);
        dialog.dismiss();
    }

    public void setNegativeButton(String text, OnClickListener listener) {
        builder.setNegativeButton(text, listener);
    }

    public SelectDialog show() {
        String[] items = new String[list.size()];

        for (int iLoop = 0; iLoop < items.length; iLoop++) {
            items[iLoop] = list.get(iLoop).second;
        }
        builder.setSingleChoiceItems(items, seletedIndex, this);
        AlertDialog dialog = builder.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.7f;
        window.setAttributes(lp);
        dialog.show();
        return this;
    }
}
