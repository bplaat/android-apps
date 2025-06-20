/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.components;

import java.util.Objects;

import javax.annotation.Nullable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.SeekBar;

import nl.plaatsoft.bassiemusic.models.Music;
import nl.plaatsoft.bassiemusic.tasks.FetchCoverTask;
import nl.plaatsoft.bassiemusic.R;

public class MusicPlayer extends LinearLayout {
    private static final int PREVIOUS_RESET_TIMEOUT = 25000;
    private static final int SEEK_SKIP_TIME = 10000;
    private static final int SYNC_TIMEOUT = 200;

    public static interface OnInfoClickListener {
        public void onInfoClick();
    }

    public static interface OnPreviousListener {
        public void onPrevious(boolean inHistory);
    }

    public static interface OnNextListener {
        public void onNext(boolean inHistory);
    }

    private IntentFilter becomingNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        public void onReceive(@SuppressWarnings("null") Context context, @SuppressWarnings("null") Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
        }
    };
    private boolean becomingNoisyIsRegistered = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private @SuppressWarnings("null") PowerManager.WakeLock wakeLock;
    private @SuppressWarnings("null") MediaPlayer mediaPlayer;
    private @SuppressWarnings("null") Runnable syncUserInterfaceInterval;

    private @SuppressWarnings("null") ImageButton playButton;
    private @SuppressWarnings("null") TextSwitcher timeCurrentLabel;
    private @SuppressWarnings("null") TextSwitcher timeUntilLabel;
    private @SuppressWarnings("null") SeekBar seekBar;

    private @Nullable OnInfoClickListener onInfoClickListener;
    private @Nullable OnPreviousListener onPreviousListener;
    private @Nullable OnNextListener onNextListener;

    private @Nullable Music playingMusic;
    private int requestStartPosition;
    private boolean requestAutoPlayed;

    public MusicPlayer(Context context) {
        this(context, null);
    }

    @SuppressWarnings("this-escape")
    public MusicPlayer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_music_player, this);

        var powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BassieMusic::WakeLock");

        syncUserInterfaceInterval = () -> {
            syncUserInterface();
            handler.postDelayed(syncUserInterfaceInterval, SYNC_TIMEOUT);
        };

        findViewById(R.id.music_player_info_button).setOnClickListener(v -> {
            if (onInfoClickListener != null)
                onInfoClickListener.onInfoClick();
        });

        var previousButton = findViewById(R.id.music_player_previous_button);
        previousButton.setOnClickListener(v -> {
            if (getCurrentPosition() > PREVIOUS_RESET_TIMEOUT) {
                seekTo(0);
            } else {
                if (onPreviousListener != null)
                    onPreviousListener.onPrevious(false);
            }
        });
        previousButton.setOnLongClickListener(v -> {
            if (onPreviousListener != null)
                onPreviousListener.onPrevious(true);
            return true;
        });

        findViewById(R.id.music_player_seek_back_button).setOnClickListener(v -> {
            seekTo(Math.max(getCurrentPosition() - SEEK_SKIP_TIME, 0));
        });

        playButton = findViewById(R.id.music_player_play_button);
        playButton.setOnClickListener(v -> {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        });

        findViewById(R.id.music_player_seek_forward_button).setOnClickListener(v -> {
            seekTo(Math.min(getCurrentPosition() + SEEK_SKIP_TIME, mediaPlayer.getDuration()));
        });

        var nextButton = findViewById(R.id.music_player_next_button);
        nextButton.setOnClickListener(v -> {
            if (onNextListener != null)
                onNextListener.onNext(false);
        });
        nextButton.setOnLongClickListener(v -> {
            if (onNextListener != null)
                onNextListener.onNext(true);
            return true;
        });

        timeCurrentLabel = findViewById(R.id.music_player_time_current_label);
        timeUntilLabel = findViewById(R.id.music_player_time_until_label);
        seekBar = findViewById(R.id.music_player_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(@SuppressWarnings("null") SeekBar seekBar) {
                handler.removeCallbacks(syncUserInterfaceInterval);
            }

            public void onProgressChanged(@SuppressWarnings("null") SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    timeCurrentLabel.setCurrentText(Music.formatDuration(progress));
                    timeUntilLabel.setCurrentText("-" + Music.formatDuration(mediaPlayer.getDuration() - progress));
                }
            }

            public void onStopTrackingTouch(@SuppressWarnings("null") SeekBar seekBar) {
                seekTo(seekBar.getProgress());
            }
        });

        mediaPlayer = new MediaPlayer();

        var infoCoverImage = (ImageView) findViewById(R.id.music_player_info_cover_image);
        var infoTitleLabel = (TextSwitcher) findViewById(R.id.music_player_info_title_label);
        var infoArtistsLabel = (TextSwitcher) findViewById(R.id.music_player_info_artists_label);
        var infoDurationLabel = (TextSwitcher) findViewById(R.id.music_player_info_duration_label);
        mediaPlayer.setOnPreparedListener((MediaPlayer mediaPlayer) -> {
            // Update info texts
            FetchCoverTask.with(getContext()).fromMusic(Objects.requireNonNull(playingMusic)).fadeIn()
                    .into(infoCoverImage).fetch();

            var newTitleLabel = Objects.requireNonNull(playingMusic).getTitle();
            if (!((TextView) infoTitleLabel.getCurrentView()).getText().equals(newTitleLabel)) {
                infoTitleLabel.setText(newTitleLabel);
            }
            infoTitleLabel.setSelected(true);

            var newArtistsLabel = String.join(", ", Objects.requireNonNull(playingMusic).getArtists());
            if (!((TextView) infoArtistsLabel.getCurrentView()).getText().equals(newArtistsLabel)) {
                infoArtistsLabel.setText(newArtistsLabel);
            }
            infoArtistsLabel.setSelected(true);

            var newDurationLabel = Music.formatDuration(Objects.requireNonNull(playingMusic).getDuration());
            if (!((TextView) infoDurationLabel.getCurrentView()).getText().equals(newDurationLabel)) {
                infoDurationLabel.setText(newDurationLabel);
            }

            // Update seekbar max
            seekBar.setMax(mediaPlayer.getDuration());

            // Seek media player to request start position
            if (requestStartPosition != 0) {
                seekTo(requestStartPosition, false);
            }

            syncUserInterface();

            // Start media player when autoplayed
            if (requestAutoPlayed) {
                play();
            } else {
                pause();
            }
        });

        mediaPlayer.setOnCompletionListener((MediaPlayer mediaPlayer) -> {
            if (onNextListener != null)
                onNextListener.onNext(false);
        });
    }

    public void setOnInfoClickListener(OnInfoClickListener onInfoClickListener) {
        this.onInfoClickListener = onInfoClickListener;
    }

    public void setOnPreviousListener(OnPreviousListener onPreviousListener) {
        this.onPreviousListener = onPreviousListener;
    }

    public void setOnNextListener(OnNextListener onNextListener) {
        this.onNextListener = onNextListener;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    private void syncUserInterface() {
        timeCurrentLabel.setCurrentText(Music.formatDuration(getCurrentPosition()));

        timeUntilLabel.setCurrentText("-" + Music.formatDuration(mediaPlayer.getDuration() - getCurrentPosition()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar.setProgress(getCurrentPosition(), true);
        } else {
            seekBar.setProgress(getCurrentPosition());
        }
    }

    public void seekTo(int position) {
        seekTo(position, true);
    }

    public void seekTo(int position, boolean doPauseCheck) {
        // Seek to position
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(position, MediaPlayer.SEEK_CLOSEST_SYNC);
        } else {
            mediaPlayer.seekTo(position);
        }

        // Un pause music when paused
        if (doPauseCheck) {
            if (!isPlaying()) {
                play();
            } else {
                handler.removeCallbacks(syncUserInterfaceInterval);
                handler.post(syncUserInterfaceInterval);
            }
        }
    }

    public void loadMusic(Music music, int startPosition, boolean isAutoPlayed) {
        handler.removeCallbacks(syncUserInterfaceInterval);

        // Set music info and request vars
        playingMusic = music;
        requestStartPosition = startPosition;
        requestAutoPlayed = isAutoPlayed;

        // Reset and prepare media player
        mediaPlayer.reset();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(getContext(), Objects.requireNonNull(playingMusic).getContentUri());
            mediaPlayer.prepareAsync();
        } catch (Exception exception) {
            Log.e(getContext().getPackageName(), "An exception catched!", exception);
        }
    }

    public void release() {
        handler.removeCallbacks(syncUserInterfaceInterval);

        mediaPlayer.release();

        if (becomingNoisyIsRegistered) {
            getContext().unregisterReceiver(becomingNoisyReceiver);
            becomingNoisyIsRegistered = false;
        }

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void play() {
        handler.removeCallbacks(syncUserInterfaceInterval);

        playButton.setImageResource(R.drawable.ic_pause);

        if (!becomingNoisyIsRegistered) {
            getContext().registerReceiver(becomingNoisyReceiver, becomingNoisyFilter);
            becomingNoisyIsRegistered = true;
        }

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        if (!isPlaying()) {
            mediaPlayer.start();
        }

        handler.post(syncUserInterfaceInterval);
    }

    public void pause() {
        handler.removeCallbacks(syncUserInterfaceInterval);

        playButton.setImageResource(R.drawable.ic_play);

        if (isPlaying()) {
            mediaPlayer.pause();
        }

        if (becomingNoisyIsRegistered) {
            getContext().unregisterReceiver(becomingNoisyReceiver);
            becomingNoisyIsRegistered = false;
        }

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
