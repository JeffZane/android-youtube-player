package com.pierfrancescosoffritti.androidyoutubeplayer.core.sampleapp.examples.recyclerViewExample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.sampleapp.YouTubePlayerUiController;
import com.pierfrancescosoffritti.aytplayersample.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Activity activity;
    private String[] videoIds;
    private Lifecycle lifecycle;
    private RecyclerView mRecyclerView;
    private YouTubePlayerView mYouTubePlayerView;
    private YouTubePlayer mYouTubePlayer;
    private int mCurrentPosition = -1;

    RecyclerViewAdapter(AppCompatActivity activity, String[] videoIds, Lifecycle lifecycle) {
        this.activity = activity;
        this.videoIds = videoIds;
        this.lifecycle = lifecycle;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                FrameLayout flRoot = view.findViewById(R.id.fl_root);
                if (flRoot != null) {
                    // 隐藏上一播放item的loading按钮，显示play按钮
                    view.findViewById(R.id.loading_view).setVisibility(View.GONE);
                    view.findViewById(R.id.iv_play).setVisibility(View.VISIBLE);

                    View childAt = flRoot.getChildAt(1);
                    if (childAt instanceof YouTubePlayerView) {
                        mCurrentPosition = -1;
                        int position = mRecyclerView.getChildAdapterPosition(flRoot);
                        if (mYouTubePlayer != null) {
                            mYouTubePlayer.pause();
                            Log.d("yzg888", "onChildViewDetachedFromWindow: pausePlayer, 移除position=" + position + "的YouTubePlayerView");
                        } else {
                            Log.d("yzg888", "onChildViewDetachedFromWindow: 移除position=" + position + "的YouTubePlayerView");
                        }
                        removeView(childAt);
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.setData(videoIds[position]);
    }

    @Override
    public int getItemCount() {
        return videoIds.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout flRoot;
        private TextView text;
        private View ivPlay;
        private View loadingView;

        private String currentVideoId;

        ViewHolder(View view) {
            super(view);
            flRoot = view.findViewById(R.id.fl_root);
            text = view.findViewById(R.id.text);
            ivPlay = view.findViewById(R.id.iv_play);
            loadingView = view.findViewById(R.id.loading_view);
        }

        @SuppressLint("DefaultLocale")
        void setData(String videoId) {
            currentVideoId = videoId;

            text.setText(String.format("%d", getLayoutPosition()));

            loadingView.setVisibility(View.GONE);
            ivPlay.setVisibility(View.VISIBLE);
            ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 隐藏上一播放item的loading按钮，显示play按钮
                    if (mCurrentPosition >= 0) {
                        ViewHolder viewHolder = (ViewHolder) mRecyclerView.findViewHolderForLayoutPosition(mCurrentPosition);
                        if (viewHolder != null) {
                            viewHolder.loadingView.setVisibility(View.GONE);
                            viewHolder.ivPlay.setVisibility(View.VISIBLE);
                        }
                    }

                    loadingView.setVisibility(View.VISIBLE);
                    ivPlay.setVisibility(View.GONE);

                    mCurrentPosition = getLayoutPosition();
                    Log.d("yzg888", "onClick: " + mCurrentPosition);
                    if (mYouTubePlayerView != null && mYouTubePlayer != null) {
                        Log.d("yzg888", "mYouTubePlayerView != null && mYouTubePlayer != null");
                        mYouTubePlayer.pause();
                        removeView(mYouTubePlayerView);
                        mYouTubePlayer.cueVideo(currentVideoId, 0);
                        mYouTubePlayer.play();
                    } else {
                        if (mYouTubePlayerView != null) {
                            mYouTubePlayerView.release();
                            removeView(mYouTubePlayerView);
                        }
                        Log.d("yzg888", "onClick: " + (mYouTubePlayerView == null) + " " + (mYouTubePlayer == null));
                        mYouTubePlayerView = new YouTubePlayerView(v.getContext());
                        lifecycle.addObserver(mYouTubePlayerView);
                        mYouTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                            @Override
                            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                View customPlayerUi = mYouTubePlayerView.inflateCustomPlayerUi(R.layout.nb_youtube_custom_player_ui);
                                YouTubePlayerUiController customPlayerUiController = new YouTubePlayerUiController(activity, customPlayerUi, youTubePlayer, mYouTubePlayerView);
                                youTubePlayer.addListener(customPlayerUiController);
                                mYouTubePlayerView.addFullScreenListener(customPlayerUiController);

                                mYouTubePlayer = youTubePlayer;
                                if (mCurrentPosition == getLayoutPosition()) {
                                    Log.d("yzg888", "onReady: current position");
                                    youTubePlayer.cueVideo(currentVideoId, 0);
                                    youTubePlayer.play();
                                } else {
                                    Log.d("yzg888", "onReady: " + getLayoutPosition() + " is not current position " + mCurrentPosition);
                                }
                            }

                            @Override
                            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                                Log.d("yzg888", "onStateChange: " + state.name() + " position=" + getLayoutPosition());
                                ViewHolder viewHolder = (ViewHolder) mRecyclerView.findViewHolderForLayoutPosition(mCurrentPosition);
                                if (viewHolder != null && state == PlayerConstants.PlayerState.VIDEO_CUED) {
                                    if (viewHolder.flRoot.getChildAt(1) instanceof YouTubePlayerView) {
                                        Log.d("yzg888", "onStateChange VIDEO_CUED: YouTubePlayerView has been added");
                                    } else {
                                        viewHolder.flRoot.addView(mYouTubePlayerView);
                                        Log.d("yzg888", "onStateChange VIDEO_CUED: add YouTubePlayerView");
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void removeView(@NonNull View view) {
        ViewParent viewParent = view.getParent();
        if (viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeView(view);
        }
    }
}
