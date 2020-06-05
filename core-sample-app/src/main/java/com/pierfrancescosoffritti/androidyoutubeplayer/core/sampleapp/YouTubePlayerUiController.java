package com.pierfrancescosoffritti.androidyoutubeplayer.core.sampleapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.FadeViewHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener;
import com.pierfrancescosoffritti.aytplayersample.R;

import androidx.annotation.NonNull;

public class YouTubePlayerUiController extends AbstractYouTubePlayerListener implements YouTubePlayerFullScreenListener, YouTubePlayerSeekBarListener {

    private View mPlayerUi;
    private Activity mActivity;
    private YouTubePlayer mYouTubePlayer;
    private YouTubePlayerView mYouTubePlayerView;
    private View mPanel;
    private ProgressBar mProgressBar;
    private ImageView mPlayPauseButton;
    private LinearLayout mBottomBar;
    private ImageView mFullscreenButton;
    private YouTubePlayerTracker mYouTubePlayerTracker;
    private boolean mIsFullscreen = false;
    private FadeViewHelper mFadeControlsContainer;
    private boolean mIsPlaying;
    private PlayerConstants.PlayerState mCurrentState;

    public YouTubePlayerUiController(Activity activity, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        mPlayerUi = customPlayerUi;
        mActivity = activity;
        mYouTubePlayer = youTubePlayer;
        mYouTubePlayerView = youTubePlayerView;

        mYouTubePlayerTracker = new YouTubePlayerTracker();
        youTubePlayer.addListener(mYouTubePlayerTracker);
        initViews(customPlayerUi);
    }

    private void initViews(View playerUi) {
        mPanel = playerUi.findViewById(R.id.panel);
        mPanel.setOnClickListener(v -> {
            if (!isShowProgressBar()) {
                mPlayPauseButton.setVisibility(View.VISIBLE);
            }
            if (isShowBottomBar()) {
                mBottomBar.setVisibility(View.VISIBLE);
            }
            mFadeControlsContainer.toggleVisibility();
        });

        RelativeLayout rlContainer = playerUi.findViewById(R.id.rl_container);
        mFadeControlsContainer = new FadeViewHelper(rlContainer);
        mYouTubePlayer.addListener(mFadeControlsContainer);

        mProgressBar = playerUi.findViewById(R.id.pb_loading);

        mBottomBar = playerUi.findViewById(R.id.ll_bottom_bar);
        YouTubePlayerSeekBar youTubePlayerSeekBar = playerUi.findViewById(R.id.ytb_player_seekbar);
        youTubePlayerSeekBar.setYoutubePlayerSeekBarListener(this);
        mYouTubePlayer.addListener(youTubePlayerSeekBar);

        mFullscreenButton = playerUi.findViewById(R.id.fullscreen_button);
        mFullscreenButton.setOnClickListener(v -> {
            if (mIsFullscreen) {
                mYouTubePlayerView.exitFullScreen();
            } else {
                mYouTubePlayerView.enterFullScreen();
            }
        });

        mPlayPauseButton = playerUi.findViewById(R.id.play_pause_button);
        mPlayPauseButton.setOnClickListener(v -> {
            if (mIsPlaying) {
                mYouTubePlayer.pause();
            } else {
                mYouTubePlayer.play();
            }
        });

        mPlayPauseButton.setOnClickListener((view) -> {
            if (mYouTubePlayerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
                mYouTubePlayer.pause();
            } else {
                mYouTubePlayer.play();
            }
        });
    }

    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
        mCurrentState = state;
        mIsPlaying = mCurrentState == PlayerConstants.PlayerState.PLAYING;
        boolean isShowProgressBar = isShowProgressBar();
        Log.d("FragmentVideoList", "onStateChange: " + mCurrentState + " isShowProgressBar: " + isShowProgressBar);

        mPlayPauseButton.setImageResource(mCurrentState == PlayerConstants.PlayerState.PLAYING ?
                R.drawable.nb_ic_pause_button : R.drawable.nb_ic_play_button);
        mProgressBar.setVisibility(isShowProgressBar ? View.VISIBLE : View.GONE);

        if (isShowProgressBar) {
            mPlayPauseButton.setVisibility(View.GONE);
            mBottomBar.setVisibility(View.GONE);
        }

//        mPanel.setBackgroundColor(ContextCompat.getColor(mActivity,
//                (mCurrentState == PlayerConstants.PlayerState.UNKNOWN ||
//                        mCurrentState == PlayerConstants.PlayerState.UNSTARTED) ?
//                        android.R.color.black : android.R.color.transparent));
    }

    private boolean isShowProgressBar() {
        return mCurrentState == PlayerConstants.PlayerState.UNSTARTED ||
                mCurrentState == PlayerConstants.PlayerState.VIDEO_CUED ||
                mCurrentState == PlayerConstants.PlayerState.BUFFERING;
    }

    private boolean isShowBottomBar() {
        return mCurrentState == PlayerConstants.PlayerState.PLAYING ||
                mCurrentState == PlayerConstants.PlayerState.PAUSED ||
                mCurrentState == PlayerConstants.PlayerState.BUFFERING;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
    }

    @Override
    public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
        mPlayPauseButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mBottomBar.setVisibility(View.GONE);
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        mIsFullscreen = true;
        mFullscreenButton.setImageResource(R.drawable.nb_sel_exit_fullscreen);

        ViewGroup.LayoutParams viewParams = mPlayerUi.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mPlayerUi.setLayoutParams(viewParams);

        mActivity.setRequestedOrientation(mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        mIsFullscreen = false;
        mFullscreenButton.setImageResource(R.drawable.nb_sel_fullscreen_mode);

        ViewGroup.LayoutParams viewParams = mPlayerUi.getLayoutParams();
        viewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mPlayerUi.setLayoutParams(viewParams);

        mActivity.setRequestedOrientation(mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void seekTo(float time) {
        mYouTubePlayer.seekTo(time);
    }

    public void setFullscreenEnable(boolean enable) {
        mFullscreenButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }
}
