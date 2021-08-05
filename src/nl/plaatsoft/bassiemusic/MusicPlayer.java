package nl.plaatsoft.bassiemusic;

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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.SeekBar;

public class MusicPlayer extends LinearLayout {
    public static interface OnInfoClickListener {
        public void onInfoClick();
    }

    public static interface OnPreviousListener {
        public void onPrevious();
    }

    public static interface OnNextListener {
        public void onNext();
    }

    private IntentFilter becomingNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BroadcastReceiver becomingNoisyReceiver;
    private boolean becomingNoisyIsRegistered = false;

    private PowerManager.WakeLock wakeLock;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable syncUserInterfaceInterval;

    private ImageButton playButton;
    private TextSwitcher timeCurrentLabel;
    private TextSwitcher timeUntilLabel;
    private SeekBar seekBar;

    private OnInfoClickListener onInfoClickListener;
    private OnPreviousListener onPreviousListener;
    private OnNextListener onNextListener;

    private Music playingMusic;
    private int requestStartPosition;
    private boolean requestAutoPlayed;

    public MusicPlayer(Context context) {
        super(context);
        initView();
    }

    public MusicPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.view_music_player, this);

        becomingNoisyReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                    pause();
                }
            }
        };

        PowerManager powerManager = (PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BassieMusic::WakeLock");

        handler = new Handler(Looper.getMainLooper());
        syncUserInterfaceInterval = () -> {
            syncUserInterface();
            handler.postDelayed(syncUserInterfaceInterval, Config.MUSIC_PLAYER_SYNC_TIMEOUT);
        };

        ((LinearLayout)findViewById(R.id.music_player_info_button)).setOnClickListener((View view) -> {
            onInfoClickListener.onInfoClick();
        });

        ((ImageButton)findViewById(R.id.music_player_previous_button)).setOnClickListener((View view) -> {
            if (getCurrentPosition() > Config.MUSIC_PLAYER_PREVIOUS_RESET_TIMEOUT) {
                seekTo(0);
            } else {
                onPreviousListener.onPrevious();
            }
        });

        ((ImageButton)findViewById(R.id.music_player_seek_back_button)).setOnClickListener((View view) -> {
            seekTo(Math.max(getCurrentPosition() - Config.MUSIC_PLAYER_SEEK_SKIP_TIME, 0));
        });

        playButton = (ImageButton)findViewById(R.id.music_player_play_button);
        playButton.setOnClickListener((View view) -> {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        });

        ((ImageButton)findViewById(R.id.music_player_seek_forward_button)).setOnClickListener((View view) -> {
            seekTo(Math.min(getCurrentPosition() + Config.MUSIC_PLAYER_SEEK_SKIP_TIME, mediaPlayer.getDuration()));
        });

        ((ImageButton)findViewById(R.id.music_player_next_button)).setOnClickListener((View view) -> {
            onNextListener.onNext();
        });

        timeCurrentLabel = (TextSwitcher)findViewById(R.id.music_player_time_current_label);
        timeUntilLabel = (TextSwitcher)findViewById(R.id.music_player_time_until_label);
        seekBar = (SeekBar)findViewById(R.id.music_player_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(syncUserInterfaceInterval);
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    timeCurrentLabel.setCurrentText(Music.formatDuration(progress));
                    timeUntilLabel.setCurrentText("-" + Music.formatDuration(mediaPlayer.getDuration() - progress));
                }
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
            }
        });

        mediaPlayer = new MediaPlayer();

        TextSwitcher infoTitleLabel = (TextSwitcher)findViewById(R.id.music_player_info_title_label);
        TextSwitcher infoDurationLabel = (TextSwitcher)findViewById(R.id.music_player_info_duration_label);
        mediaPlayer.setOnPreparedListener((MediaPlayer mediaPlayer) -> {
            // Update info texts
            infoTitleLabel.setText(playingMusic.getTitle());
            infoTitleLabel.setSelected(true);

            infoDurationLabel.setText(Music.formatDuration(playingMusic.getDuration()));

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
            onNextListener.onNext();
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
            mediaPlayer.setDataSource(getContext(), playingMusic.getUri());
            mediaPlayer.prepareAsync();
        } catch (Exception exception) {
            exception.printStackTrace();
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
