package com.codenameakshay.async_wallpaper;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends Engine {
        private SimpleExoPlayer exoPlayer;

        @SuppressLint("SdCardPath")
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            initializeExoPlayer(holder);
        }

        private void initializeExoPlayer(SurfaceHolder holder) {
            try {
                if (exoPlayer == null) {
                    SharedPreferences sharedPreferences = getSharedPreferences("WallpaperPreferences", Context.MODE_PRIVATE);

                    exoPlayer = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                    PlayerView playerView = new PlayerView(getApplicationContext());
                    playerView.setPlayer(exoPlayer);
                    playerView.setUseController(false);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    exoPlayer.setVideoSurfaceHolder(holder);

                    // Uri videoUri = Uri.parse("asset:///test_video.mp4");
                    // MediaItem mediaItem = MediaItem.fromUri(videoUri);
                    Uri videoUri = Uri.parse(getFilesDir() + "/file.mp4");
                    MediaItem mediaItem = MediaItem.fromUri(videoUri);
                    exoPlayer.setMediaItem(mediaItem);
                    exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                    exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    exoPlayer.setPlaybackSpeed(sharedPreferences.getFloat("playbackSpeed", 1.0f));
                    exoPlayer.setVolume(sharedPreferences.getBoolean("isAudioEnabled", false) ? 1.0f : 0);
                    exoPlayer.prepare();
                    exoPlayer.play();
                    Log.d("VideoEngine", "ExoPlayer started successfully");
                }
            } catch (Exception e) {
                Log.e("VideoEngine", "Error initializing ExoPlayer", e);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (exoPlayer != null) {
                if (visible) {
                    exoPlayer.play();
                } else {
                    exoPlayer.pause();
                }
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (exoPlayer != null) {
                exoPlayer.release();
                exoPlayer = null;
            }
        }
    }
}

