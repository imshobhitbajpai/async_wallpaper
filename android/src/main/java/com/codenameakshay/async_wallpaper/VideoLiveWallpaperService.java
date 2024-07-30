package com.codenameakshay.async_wallpaper;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.IOException;

public class VideoLiveWallpaperService extends WallpaperService {

    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.codenameakshay.async_wallpaper";
    public static final String KEY_ACTION = "music";
    public static final boolean ACTION_MUSIC_UNMUTE = false;
    public static final boolean ACTION_MUSIC_MUTE = true;

    public static void setToWallPaper(Context context) {
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, VideoLiveWallpaperService.class));
        context.startActivity(intent);
        try {
            WallpaperManager.getInstance(context).clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    class VideoEngine extends Engine {
        private SimpleExoPlayer exoPlayer;
        private PlayerView playerView;
        private BroadcastReceiver broadcastReceiver;

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter intentFilter = new IntentFilter(VideoLiveWallpaperService.VIDEO_PARAMS_CONTROL_ACTION);
            registerReceiver(broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean action = intent.getBooleanExtra(KEY_ACTION, false);
                    if (exoPlayer != null) {
                        if (action) {
                            exoPlayer.setVolume(0);
                        } else {
                            exoPlayer.setVolume(1.0f);
                        }
                    }
                }
            }, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        }

        @SuppressLint("SdCardPath")
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            initializeExoPlayer(holder);
        }

        private void initializeExoPlayer(SurfaceHolder holder) {
            try {
                if (exoPlayer == null) {
                    exoPlayer = new SimpleExoPlayer.Builder(getApplicationContext()).build();

                    // Create and configure PlayerView
                    playerView = new PlayerView(getApplicationContext());
                    playerView.setPlayer(exoPlayer);
                    playerView.setUseController(false);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

                    // Set the SurfaceView of PlayerView to the provided SurfaceHolder
                    exoPlayer.setVideoSurfaceHolder(holder);

                    // Uri videoUri = Uri.parse("asset:///test_video.mp4");
                    // MediaItem mediaItem = MediaItem.fromUri(videoUri);
                    Uri videoUri = Uri.parse(getFilesDir() + "/file.mp4");
                    MediaItem mediaItem = MediaItem.fromUri(videoUri);
                    exoPlayer.setMediaItem(mediaItem);
                    exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                    exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    exoPlayer.prepare();
                    exoPlayer.play();
                    exoPlayer.setVolume(0);
                    Log.d("VideoEngine", "ExoPlayer started successfully");
                }
            } catch (Exception e) {
                Log.e("VideoEngine", "Error initializing ExoPlayer", e);
                e.printStackTrace();
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
            unregisterReceiver(broadcastReceiver);
        }
    }
}
