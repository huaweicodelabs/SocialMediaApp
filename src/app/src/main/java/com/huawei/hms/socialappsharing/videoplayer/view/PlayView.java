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

package com.huawei.hms.socialappsharing.videoplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.instreamad.InstreamAd;
import com.huawei.hms.ads.instreamad.InstreamAdLoadListener;
import com.huawei.hms.ads.instreamad.InstreamAdLoader;
import com.huawei.hms.ads.instreamad.InstreamMediaStateListener;
import com.huawei.hms.ads.instreamad.InstreamView;
import com.huawei.hms.socialappsharing.R;
import com.huawei.hms.socialappsharing.videoplayer.adapter.SelectPlayDataAdapter;
import com.huawei.hms.socialappsharing.videoplayer.contract.OnPlayWindowListener;
import com.huawei.hms.socialappsharing.videoplayer.contract.OnWisePlayerListener;
import com.huawei.hms.socialappsharing.videoplayer.entity.PlayEntity;
import com.huawei.hms.socialappsharing.videoplayer.utils.Constants;
import com.huawei.hms.socialappsharing.videoplayer.utils.DataFormatUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.DeviceUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.DialogUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.LogUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.PlayControlUtil;
import com.huawei.hms.socialappsharing.videoplayer.utils.TimeUtil;
import com.huawei.hms.videokit.player.WisePlayer;

import java.util.Iterator;
import java.util.List;

/**
 * Play View
 */
public class PlayView {
    // Context
    private final Context context;

    // Play SurfaceView
    private SurfaceView surfaceView;

    // Play TextureView
    private TextureView textureView;

    // Play seekBar
    private SeekBar seekBar;

    // Current play time
    private TextView currentTimeTv;

    // Current play total time
    private TextView totalTimeTv;

    // Play/Stop button
    private ImageView playImg;

    // Video buffer view
    private RelativeLayout videoBufferLayout;

    // Video buffer load percentage
    private TextView bufferPercentTv;

    // Video name
    private TextView currentPlayNameTv;

    // Play speed value
    private TextView speedTv;

    // Full screen Button
    private Button fullScreenBt;

    // Video action button layout
    private FrameLayout controlLayout;

    // Video bottom layout
    private LinearLayout contentLayout;

    // Video name
    private TextView videoNameTv;

    // Video width and height
    private TextView videoSizeTv;

    // Video play total time
    private TextView videoTimeTv;

    // Buffer progress
    private TextView videoDownloadTv;

    // Video of the current rate
    private TextView videoBitrateTv;

    // adapter
    private SelectPlayDataAdapter selectPlayDataAdapter;

    // Android listener
    private final OnPlayWindowListener onPlayWindowListener;

    // VideoKit SDK listener
    private final OnWisePlayerListener onWisePlayerListener;

    // Switch bitrate
    private TextView switchBitrateTv;

    // Switching bitrate prompt
    private TextView switchingBitrateTv;
    private RelativeLayout instreamContainer;
    private InstreamView instreamView;

    private int maxAdDuration;
    private WisePlayer wisePlayer;
    private TextView countDown;
    private InstreamAdLoader adLoader;

    /**
     * Constructor
     *
     * @param context              Context
     * @param onPlayWindowListener Android listener
     * @param onWisePlayerListener VideoKit SDK listener
     */
    public PlayView(Context context, OnPlayWindowListener onPlayWindowListener,
                    OnWisePlayerListener onWisePlayerListener) {
        this.context = context;
        this.onPlayWindowListener = onPlayWindowListener;
        this.onWisePlayerListener = onWisePlayerListener;
    }

    /**
     * Get parent view
     *
     * @return Parent view
     */
    public View getContentView() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.play_view, null);
        initView(view);
        initRecycleView(view);
        showDefaultValueView();
        return view;
    }

    /**
     * Init view
     *
     * @param view Parent view
     */
    private void initView(View view) {
        if (view != null) {
            surfaceView = view.findViewById(R.id.surface_view);
            textureView = view.findViewById(R.id.texture_view);
            if (PlayControlUtil.isSurfaceView()) {
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.addCallback(onPlayWindowListener);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                textureView.setVisibility(View.GONE);
                surfaceView.setVisibility(View.VISIBLE);
            } else {
                textureView.setSurfaceTextureListener(onPlayWindowListener);
                textureView.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE);
            }
            seekBar = view.findViewById(R.id.seek_bar);
            seekBar.setOnSeekBarChangeListener(onWisePlayerListener);
            currentTimeTv = view.findViewById(R.id.current_time_tv);
            totalTimeTv = view.findViewById(R.id.total_time_tv);
            playImg = view.findViewById(R.id.play_btn);
            playImg.setOnClickListener(onPlayWindowListener);
            // refresh button
            ImageView playRefreshImg = view.findViewById(R.id.play_refresh);
            playRefreshImg.setOnClickListener(onPlayWindowListener);
            // Back button
            TextView backTv = view.findViewById(R.id.back_tv);
            backTv.setOnClickListener(onPlayWindowListener);
            fullScreenBt = view.findViewById(R.id.fullscreen_btn);
            fullScreenBt.setOnClickListener(onPlayWindowListener);
            videoBufferLayout = view.findViewById(R.id.buffer_rl);
            videoBufferLayout.setVisibility(View.GONE);
            controlLayout = view.findViewById(R.id.control_layout);
            bufferPercentTv = view.findViewById(R.id.play_process_buffer);
            contentLayout = view.findViewById(R.id.content_layout);
            currentPlayNameTv = view.findViewById(R.id.video_name_tv);
            speedTv = view.findViewById(R.id.play_speed_btn);
            speedTv.setOnClickListener(onPlayWindowListener);
            // Setting
            TextView settingsTv = view.findViewById(R.id.setting_tv);
            settingsTv.setOnClickListener(onPlayWindowListener);
            videoNameTv = view.findViewById(R.id.tv_video_name);
            videoSizeTv = view.findViewById(R.id.video_width_and_height);
            videoTimeTv = view.findViewById(R.id.video_time);
            videoDownloadTv = view.findViewById(R.id.video_download_speed);
            videoBitrateTv = view.findViewById(R.id.video_bitrate);
            switchBitrateTv = view.findViewById(R.id.switch_bitrate_tv);
            switchBitrateTv.setOnClickListener(onPlayWindowListener);
            switchBitrateTv.setVisibility(View.GONE);
            switchingBitrateTv = view.findViewById(R.id.switching_bitrate_tv);
            switchingBitrateTv.setVisibility(View.GONE);

            TextView skipAd = view.findViewById(R.id.instream_skip);
            countDown = view.findViewById(R.id.instream_count_down);

            skipAd.setOnClickListener(v -> {
                if (null != instreamView) {
                    instreamView.onClose();
                    instreamView.destroy();

                    hideAdViews(wisePlayer);
                }
            });

            instreamContainer = view.findViewById(R.id.instream_ad_container);
            instreamView = view.findViewById(R.id.instream_view);

            // Initialize the HUAWEI Ads SDK.
            HwAds.init(context);
            instreamView.setInstreamMediaStateListener(mediaStateListener);

        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCountDown(long playTime) {
        final String time = String.valueOf(Math.round((maxAdDuration - playTime) / 1000));
        countDown.setText(time + "s");
    }

    /**
     * media State Listener
     */
    private final InstreamMediaStateListener mediaStateListener = new InstreamMediaStateListener() {
        @Override
        public void onMediaProgress(int per, int playTime) {
            updateCountDown(playTime);
        }

        @Override
        public void onMediaStart(int playTime) {
            updateCountDown(playTime);
        }

        @Override
        public void onMediaPause(int playTime) {
            updateCountDown(playTime);
        }

        @Override
        public void onMediaStop(int playTime) {
            updateCountDown(playTime);
        }

        @Override
        public void onMediaCompletion(int playTime) {
            updateCountDown(playTime);
            hideAdViews(wisePlayer);
        }

        @Override
        public void onMediaError(int playTime, int errorCode, int extra) {
            updateCountDown(playTime);
        }
    };

    /**
     * Init recycleView
     *
     * @param view Parent view
     */
    private void initRecycleView(View view) {
        // Video data view
        RecyclerView playRecyclerView = view.findViewById(R.id.player_recycler_view);
        playRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        selectPlayDataAdapter = new SelectPlayDataAdapter(context, onPlayWindowListener);
        playRecyclerView.setAdapter(selectPlayDataAdapter);
    }

    /**
     * Set list data
     *
     * @param list Data
     */
    public void setRecycleData(List<PlayEntity> list) {
        selectPlayDataAdapter.setSelectPlayList(list);
    }

    /**
     * Update play view
     *
     * @param wisePlayer WisePlayer
     */
    public void updatePlayView(WisePlayer wisePlayer) {
        if (wisePlayer != null) {
            int totalTime = wisePlayer.getDuration();
            LogUtil.i(String.valueOf(totalTime));
            seekBar.setMax(totalTime);
            totalTimeTv.setText(TimeUtil.formatLongToTimeStr(totalTime));
            currentTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
            seekBar.setProgress(0);
            contentLayout.setVisibility(View.GONE);

            this.wisePlayer = wisePlayer;

            configAdLoader(totalTime);

            if (null != adLoader) {
                adLoader.loadAd(new AdParam.Builder().build());
            }
        }
    }

    private void playInstreamAds(List<InstreamAd> ads) {
        maxAdDuration = getMaxInstreamDuration(ads);
        instreamContainer.setVisibility(View.VISIBLE);

        instreamView.setInstreamAds(ads);
    }

    private int getMaxInstreamDuration(List<InstreamAd> ads) {
        int duration = 0;
        for (InstreamAd ad : ads) {
            duration += ad.getDuration();
        }
        return duration;
    }

    private void configAdLoader(int totalTime) {
        /*
         * if the maximum total duration is 60 seconds and the maximum number of roll ads is eight,
         * at most four 15-second roll ads or two 30-second roll ads will be returned.
         * If the maximum total duration is 120 seconds and the maximum number of roll ads is four,
         * no more roll ads will be returned after whichever is reached.
         */
        int maxCount = 1;
        if (totalTime > 30000) {
            maxCount = 2;
        }
        totalTime = totalTime / 1000;

        InstreamAdLoader.Builder builder = new InstreamAdLoader.Builder(context, context.getString(R.string.instream_ad_id));
        adLoader = builder.setTotalDuration(totalTime).setMaxCount(maxCount).setInstreamAdLoadListener(instreamAdLoadListener).build();
    }

    /**
     * instream Ad Load Listener
     */
    private final InstreamAdLoadListener instreamAdLoadListener = new InstreamAdLoadListener() {
        @Override
        public void onAdLoaded(final List<InstreamAd> ads) {
            if (null == ads || ads.size() == 0) {
                hideAdViews(wisePlayer);
                return;
            }
            Iterator<InstreamAd> it = ads.iterator();
            while (it.hasNext()) {
                InstreamAd ad = it.next();
                if (ad.isExpired()) {
                    it.remove();
                }
            }
            if (ads.size() == 0) {
                hideAdViews(wisePlayer);
                return;
            }
            playInstreamAds(ads);
            showAdViews(wisePlayer);
        }

        @Override
        public void onAdFailed(int errorCode) {
            Toast.makeText(context, "onAdFailed: " + errorCode, Toast.LENGTH_SHORT).show();
            hideAdViews(wisePlayer);
        }
    };

    private void showAdViews(WisePlayer wisePlayer) {
        wisePlayer.start();
        instreamContainer.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.GONE);
        setPlayView();
    }


    private void hideAdViews(WisePlayer wisePlayer) {
        wisePlayer.start();
        instreamContainer.setVisibility(View.GONE);
        surfaceView.setVisibility(View.VISIBLE);
        setPauseView();
    }

    /**
     * Update buffer
     *
     * @param percent percent
     */
    @SuppressLint("SetTextI18n")
    public void updateBufferingView(int percent) {
        LogUtil.d("show buffering view loading");
        if (videoBufferLayout.getVisibility() == View.GONE) {
            videoBufferLayout.setVisibility(View.VISIBLE);
        }
        bufferPercentTv.setText(percent + "%");
    }

    /**
     * Show buffer view
     */
    public void showBufferingView() {
        videoBufferLayout.setVisibility(View.VISIBLE);
        bufferPercentTv.setText("0%");
    }

    /**
     * Dismiss buffer view
     */
    public void dismissBufferingView() {
        LogUtil.d("dismiss buffering view loading");
        videoBufferLayout.setVisibility(View.GONE);
    }

    /**
     * Set stop background
     */
    public void setPauseView() {
        playImg.setImageResource(R.drawable.ic_full_screen_suspend_normal);
    }

    /**
     * Set start background
     */
    public void setPlayView() {
        playImg.setImageResource(R.drawable.ic_play);
    }

    public void updatePlayProgressView(int progress, int bufferPosition, long bufferingSpeed, int bitrate) {
        seekBar.setProgress(progress);
        seekBar.setSecondaryProgress(bufferPosition);
        currentTimeTv.setText(TimeUtil.formatLongToTimeStr(progress));
        videoDownloadTv.setText(context.getResources().getString(R.string.video_download_speed, bufferingSpeed));
        if (bitrate == 0) {
            videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate_empty));
        } else {
            videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate, bitrate));
        }
    }

    /**
     * Set full screen layout
     *
     * @param name Video name
     */
    public void setFullScreenView(String name) {
        fullScreenBt.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        currentPlayNameTv.setVisibility(View.VISIBLE);
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        currentPlayNameTv.setText(name);
    }

    /**
     * Set portrait layout
     */
    public void setPortraitView() {
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceUtil.dp2px(context, Constants.HEIGHT_DP)));
        fullScreenBt.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
        currentPlayNameTv.setVisibility(View.INVISIBLE);
        currentPlayNameTv.setText(null);
    }

    /**
     * Set play complete
     */
    public void updatePlayCompleteView() {
        playImg.setImageResource(R.drawable.ic_play);
        controlLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Player back to the background
     */
    public void onPause() {
        dismissBufferingView();
    }

    /**
     * Update Video view
     *
     * @param wisePlayer WisePlayer
     * @param name       Video name
     */
    public void setContentView(WisePlayer wisePlayer, String name) {
        if (wisePlayer != null) {
            videoNameTv.setText(context.getResources().getString(R.string.video_name, name));
            videoSizeTv.setText(context.getResources().getString(R.string.video_width_and_height, wisePlayer.getVideoWidth(), wisePlayer.getVideoHeight()));
            videoTimeTv.setText(context.getResources().getString(R.string.video_time, TimeUtil.formatLongToTimeStr(wisePlayer.getDuration())));
        }
    }

    /**
     * Get SurfaceView
     *
     * @return SurfaceView
     */
    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    /**
     * Get TextureView
     *
     * @return TextureView
     */
    public TextureView getTextureView() {
        return textureView;
    }

    /**
     * Show setting dialog
     *
     * @param settingType  Setting type
     * @param showTextList Setting text list
     * @param selectIndex  Default select index
     */
    public void showSettingDialog(int settingType, List<String> showTextList, int selectIndex) {
        DialogUtil.onSettingDialogSelectIndex(context, settingType, showTextList, selectIndex, onPlayWindowListener);
    }

    /**
     * Show setting dialog
     *
     * @param settingType  Setting type
     * @param showTextList Setting text list
     * @param selectValue  Default select value
     */
    public void showSettingDialogValue(int settingType, List<String> showTextList, String selectValue) {
        DialogUtil.onSettingDialogSelectValue(context, settingType, showTextList, selectValue, onPlayWindowListener);
    }

    /**
     * Show getting dialog
     *
     * @param gettingType  Getting type
     * @param showTextList Setting text list
     * @param selectIndex  Default select value
     */
    public void showGettingDialog(int gettingType, List<String> showTextList, int selectIndex) {
        DialogUtil.onGettingDialogSelectIndex(context, gettingType, showTextList, selectIndex, onPlayWindowListener);
    }

    /**
     * Set speed button text
     *
     * @param speedText speed value
     */
    public void setSpeedButtonText(String speedText) {
        speedTv.setText(speedText);
    }

    /**
     * Set default value
     */
    public void showDefaultValueView() {
        currentTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
        totalTimeTv.setText(TimeUtil.formatLongToTimeStr(0));
        speedTv.setText(context.getString(R.string._1_0x));
        videoNameTv.setText(context.getResources().getString(R.string.video_name, ""));
        videoSizeTv.setText(context.getResources().getString(R.string.video_width_and_height, 0, 0));
        videoTimeTv.setText(context.getResources().getString(R.string.video_time, TimeUtil.formatLongToTimeStr(0)));
        videoDownloadTv.setText(context.getResources().getString(R.string.video_download_speed, 0));
        videoBitrateTv.setText(context.getResources().getString(R.string.video_bitrate_empty));
    }

    /**
     * Reset video view
     */
    public void reset() {
        showBufferingView();
        playImg.setImageResource(R.drawable.ic_play);
        showDefaultValueView();
        hiddenSwitchingBitrateTextView();
        hiddenSwitchBitrateTextView();
        setSwitchBitrateTv(0);
    }

    /**
     * Show switch bitrate textView
     */
    public void showSwitchBitrateTextView() {
        if (switchBitrateTv.getVisibility() == View.GONE) {
            switchBitrateTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hidden switch bitrate textView
     */
    public void hiddenSwitchBitrateTextView() {
        if (switchBitrateTv.getVisibility() == View.VISIBLE) {
            switchBitrateTv.setVisibility(View.GONE);
        }
    }

    /**
     * set switch bitrate textView
     *
     * @param videoHeight video height
     */
    public void setSwitchBitrateTv(int videoHeight) {
        switchBitrateTv.setText(DataFormatUtil.getVideoQuality(context, videoHeight));
    }

    public void showSwitchingBitrateTextView(String textValue) {
        if (switchingBitrateTv.getVisibility() == View.GONE) {
            switchingBitrateTv.setVisibility(View.VISIBLE);
        }
        switchingBitrateTv.setText(Html.fromHtml(context.getString(R.string.resolution_switching, textValue)));
    }

    /**
     * Update textView show value
     *
     * @param textValue value
     */
    public void updateSwitchingBitrateTextView(String textValue) {
        switchingBitrateTv.setText(textValue);
    }

    /**
     * Hidden switching bitrate textView
     */
    public void hiddenSwitchingBitrateTextView() {
        if (switchingBitrateTv.getVisibility() == View.VISIBLE) {
            switchingBitrateTv.setVisibility(View.GONE);
        }
    }
}
