/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.activities;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import nl.plaatsoft.android.alerts.RatingAlert;
import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.bassiemusic.R;
import nl.plaatsoft.bassiemusic.components.MusicAdapter;
import nl.plaatsoft.bassiemusic.components.MusicPlayer;
import nl.plaatsoft.bassiemusic.models.Music;

import org.jspecify.annotations.Nullable;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;
    public static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private int oldLanguage = -1;
    private int oldTheme = -1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private @SuppressWarnings("null") LinearLayout musicPage;
    private @SuppressWarnings("null") LinearLayout emptyPage;
    private @SuppressWarnings("null") LinearLayout accessPage;

    private @SuppressWarnings("null") List<Music> music;
    private boolean isShuffling;
    private @SuppressWarnings("null") ArrayList<Long> musicHistory;
    private int musicHistoryCurrent;
    private @SuppressWarnings("null") MusicPlayer musicPlayer;
    private @SuppressWarnings("null") ListView musicList;
    private @SuppressWarnings("null") MusicAdapter musicAdapter;

    private int selectedPosition = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On back handler
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> { moveTaskToBack(false); });
        }

        // Pages
        musicPage = (LinearLayout)findViewById(R.id.main_music_page);
        emptyPage = (LinearLayout)findViewById(R.id.main_empty_page);
        accessPage = (LinearLayout)findViewById(R.id.main_access_page);
        useWindowInsets(findViewById(R.id.main_music_music_player), findViewById(R.id.main_empty_scroll),
            findViewById(R.id.main_access_scroll));

        // Music page
        musicHistory = new ArrayList<Long>();
        if (savedInstanceState != null && savedInstanceState.getLongArray("music_history") != null
            && savedInstanceState.getInt("music_history_current", -1) != -1) {
            var musicHistoryArray = savedInstanceState.getLongArray("music_history");
            for (var i = 0; i < musicHistoryArray.length; i++) {
                musicHistory.add(musicHistoryArray[i]);
            }
            musicHistoryCurrent = savedInstanceState.getInt("music_history_current");
        } else {
            musicHistoryCurrent = 0;
        }

        musicPlayer = (MusicPlayer)findViewById(R.id.main_music_music_player);

        musicPlayer.setOnInfoClickListener(() -> { scrollToMusicByPosition(selectedPosition); });

        musicPlayer.setOnPreviousListener((boolean inHistory) -> {
            if (inHistory || isShuffling) {
                if (musicHistoryCurrent > 0) {
                    musicHistoryCurrent--;
                    for (var musicItem : music) {
                        if (musicItem.getId() == musicHistory.get(musicHistoryCurrent)) {
                            playMusicByPosition(musicAdapter.getPosition(musicItem), 0, true, true);
                            return;
                        }
                    }
                } else if (isShuffling) {
                    playMusicByPosition((int)(Math.random() * musicAdapter.getCount()));
                }
            } else {
                playMusicByPosition(selectedPosition == 0 ? musicAdapter.getCount() - 1 : selectedPosition - 1);
            }
        });

        musicPlayer.setOnNextListener((boolean inHistory) -> {
            if (inHistory || isShuffling) {
                if (musicHistoryCurrent < musicHistory.size() - 1) {
                    musicHistoryCurrent++;
                    for (var musicItem : music) {
                        if (musicItem.getId() == musicHistory.get(musicHistoryCurrent)) {
                            playMusicByPosition(musicAdapter.getPosition(musicItem), 0, true, true);
                            return;
                        }
                    }
                } else if (isShuffling) {
                    playMusicByPosition((int)(Math.random() * musicAdapter.getCount()));
                }
            } else {
                playMusicByPosition(selectedPosition == musicAdapter.getCount() - 1 ? 0 : selectedPosition + 1);
            }
        });

        musicList = (ListView)findViewById(R.id.main_music_list);
        musicList.setFastScrollEnabled(settings.isFastScroll());

        musicAdapter = new MusicAdapter(this);
        musicList.setAdapter(musicAdapter);

        musicList.setOnItemClickListener(
            (AdapterView<?> adapterView, View view, int position, long id) -> { playMusicByPosition(position); });

        var musicShuffleButton = (ImageButton)findViewById(R.id.main_music_shuffle_button);
        isShuffling = settings.isShuffling();
        if (isShuffling) {
            musicShuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
        }
        musicShuffleButton.setOnClickListener(
            v -> { playMusicByPosition((int)(Math.random() * musicAdapter.getCount())); });
        musicShuffleButton.setOnLongClickListener(v -> {
            isShuffling = !isShuffling;
            if (isShuffling) {
                musicShuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
            } else {
                musicShuffleButton.setImageResource(R.drawable.ic_shuffle);
            }
            settings.setShuffling(isShuffling);
            return true;
        });

        findViewById(R.id.main_music_search_button).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, SearchActivity.class), SEARCH_ACTIVITY_REQUEST_CODE);
        });

        findViewById(R.id.main_music_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options_music, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Empty page
        View.OnClickListener refreshEmptyOnClick = v -> {
            musicPage.setVisibility(View.VISIBLE);
            emptyPage.setVisibility(View.GONE);
            loadMusic(false);
        };
        findViewById(R.id.main_empty_refresh_button).setOnClickListener(refreshEmptyOnClick);
        findViewById(R.id.main_empty_hero_button).setOnClickListener(refreshEmptyOnClick);
        findViewById(R.id.main_empty_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options_others, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Access page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
            View.OnClickListener accessOnClick = v -> {
                requestPermissions(new String[] {permission}, STORAGE_PERMISSION_REQUEST_CODE);
            };
            findViewById(R.id.main_access_refresh_button).setOnClickListener(accessOnClick);
            findViewById(R.id.main_access_hero_button).setOnClickListener(accessOnClick);
            findViewById(R.id.main_access_options_menu_button).setOnClickListener(view -> {
                var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
                optionsMenu.getMenuInflater().inflate(R.menu.options_others, optionsMenu.getMenu());
                optionsMenu.setOnMenuItemClickListener(this);
                optionsMenu.show();
            });

            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                accessPage.setVisibility(View.VISIBLE);
                musicPage.setVisibility(View.GONE);
                requestPermissions(new String[] {permission}, STORAGE_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean("is_music_playing")) {
            loadMusic(true);
        } else {
            loadMusic(false);
        }

        showAlerts();
    }

    public void showAlerts() {
        // Show rating alert
        RatingAlert.updateAndShow(this, SettingsActivity.STORE_PAGE_URL);

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/bassiemusic/bob.toml",
            SettingsActivity.STORE_PAGE_URL);
    }

    @Override
    public void onSaveInstanceState(@SuppressWarnings("null") Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        var musicHistoryArray = new long[musicHistory.size()];
        for (var i = 0; i < musicHistory.size(); i++) {
            musicHistoryArray[i] = musicHistory.get(i);
        }
        savedInstanceState.putLongArray("music_history", musicHistoryArray);
        savedInstanceState.putInt("music_history_current", musicHistoryCurrent);
        savedInstanceState.putBoolean("is_music_playing", musicPlayer.isPlaying());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @Override
    public void onPause() {
        if (settings.isRememberMusic())
            rememberMusic();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        musicPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, @SuppressWarnings("null") String[] permissions, @SuppressWarnings("null") int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            musicPage.setVisibility(View.VISIBLE);
            accessPage.setVisibility(View.GONE);
            loadMusic(false);
            showAlerts();
        }
    }

    @Override
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_reload_music) {
            var isPlaying = musicPlayer.isPlaying();
            if (isPlaying) {
                musicPlayer.pause();
            }
            rememberMusic();
            loadMusic(isPlaying);
            return true;
        }

        if (item.getItemId() == R.id.menu_options_settings) {
            openSettingsActivity();
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @SuppressWarnings("null") Intent data) {
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            var musicId = data.getLongExtra("id", -1);
            if (musicId != -1) {
                for (var musicItem : music) {
                    if (musicItem.getId() == musicId) {
                        handler.post(() -> playMusicByPosition(musicAdapter.getPosition(musicItem)));
                        return;
                    }
                }
            }
        }

        // When settings activity is closed check for restart
        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }

            // Else update fast scroll music list setting
            else {
                musicList.setFastScrollEnabled(settings.isFastScroll());
            }
        }
    }

    private void openSettingsActivity() {
        oldLanguage = settings.getLanguage();
        oldTheme = settings.getTheme();
        startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_ACTIVITY_REQUEST_CODE);
    }

    private void rememberMusic() {
        if (selectedPosition != -1) {
            var music = musicAdapter.getItem(selectedPosition);
            settings.setPlayingMusicId(music.getId());
            settings.setPlayingMusicPosition(musicPlayer.getCurrentPosition());
        }
    }

    private void loadMusic(boolean isAutoPlayed) {
        music = Music.loadMusic(this);
        musicAdapter.clear();
        musicAdapter.addAll(music);
        musicAdapter.refreshSections();

        if (music.size() == 0) {
            musicPage.setVisibility(View.GONE);
            emptyPage.setVisibility(View.VISIBLE);
        } else {
            var musicId = settings.getPlayingMusicId();
            var isMusicFound = false;
            if (musicId != -1) {
                for (var musicItem : music) {
                    if (musicItem.getId() == musicId) {
                        isMusicFound = true;
                        playMusicByPosition(musicAdapter.getPosition(musicItem), settings.getPlayingMusicPosition(),
                            isAutoPlayed, false);
                        break;
                    }
                }
            }

            if (!isMusicFound) {
                playMusicByPosition((int)(Math.random() * musicAdapter.getCount()), 0, isAutoPlayed, false);
            }
        }
    }

    private void playMusicByPosition(int position) {
        playMusicByPosition(position, 0, true, false);
    }

    private void playMusicByPosition(int position, int startPosition, boolean isAutoPlayed, boolean inHistory) {
        scrollToMusicByPosition(position);
        setSelectedPosition(position);

        var music = musicAdapter.getItem(position);
        if (musicHistoryCurrent == musicHistory.size() - 1
            && musicHistory.get(musicHistory.size() - 1) == music.getId()) {
            inHistory = true;
        }
        if (!inHistory) {
            for (int i = musicHistoryCurrent + 1; i < musicHistory.size(); i++) {
                musicHistory.remove(i);
            }
            musicHistory.add(music.getId());
            musicHistoryCurrent = musicHistory.size() - 1;
        }
        musicPlayer.loadMusic(music, startPosition, isAutoPlayed);
    }

    private void scrollToMusicByPosition(int position) {
        musicList.clearFocus();
        musicList.post(() -> {
            if (position < musicList.getFirstVisiblePosition()) {
                musicList.setSelection(position);
            }

            if (position > musicList.getLastVisiblePosition()) {
                musicList.setSelection(
                    position - (musicList.getLastVisiblePosition() - musicList.getFirstVisiblePosition() - 1));
            }
        });
    }

    private void updateSelectedView() {
        var selectedView = (LinearLayout)musicList.getChildAt(selectedPosition - musicList.getFirstVisiblePosition());
        if (selectedView != null) {
            var animation = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.selected_music_in);
            animation.setTarget(selectedView);
            animation.start();

            var musicTitle = (TextView)selectedView.findViewById(R.id.music_title);
            musicTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            musicTitle.setSelected(true);

            var musicArtists = (TextView)selectedView.findViewById(R.id.music_artists);
            musicArtists.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            musicArtists.setSelected(true);
        } else {
            // When the view doesn't exist right now try again next sweep
            musicList.post(() -> { updateSelectedView(); });
        }
    }

    private void setSelectedPosition(int selectedPosition) {
        if (this.selectedPosition != selectedPosition) {
            if (this.selectedPosition != -1) {
                var oldSelectedView =
                    (LinearLayout)musicList.getChildAt(this.selectedPosition - musicList.getFirstVisiblePosition());
                if (oldSelectedView != null) {
                    var animation = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.selected_music_out);
                    animation.setTarget(oldSelectedView);
                    animation.start();

                    var musicTitle = (TextView)oldSelectedView.findViewById(R.id.music_title);
                    musicTitle.setEllipsize(null);
                    musicTitle.setSelected(false);

                    var musicArtists = (TextView)oldSelectedView.findViewById(R.id.music_artists);
                    musicArtists.setEllipsize(null);
                    musicArtists.setSelected(false);
                }
            }

            this.selectedPosition = selectedPosition;
            musicAdapter.setSelectedPosition(selectedPosition);
            musicList.post(() -> updateSelectedView());
        }
    }
}
