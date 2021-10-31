package nl.plaatsoft.bassiemusic;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.Manifest;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;
    public static final int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private int oldLanguage = -1;
    private int oldTheme = -1;

    private Handler handler;
    private LinearLayout musicPage;
    private LinearLayout emptyPage;
    private LinearLayout accessPage;

    private List<Music> music;
    private boolean isShuffling;
    private ArrayList<Long> musicHistory;
    private int musicHistoryCurrent;
    private MusicPlayer musicPlayer;
    private ListView musicList;
    private MusicAdapter musicAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pages
        handler = new Handler(Looper.getMainLooper());

        musicPage = (LinearLayout)findViewById(R.id.main_music_page);

        emptyPage = (LinearLayout)findViewById(R.id.main_empty_page);

        accessPage = (LinearLayout)findViewById(R.id.main_access_page);

        // Music page
        musicHistory = new ArrayList<Long>();
        if (
            savedInstanceState != null &&
            savedInstanceState.getLongArray("music_history") != null &&
            savedInstanceState.getInt("music_history_current", -1) != -1
        ) {
            long[] musicHistoryArray = savedInstanceState.getLongArray("music_history");
            for (int i = 0; i < musicHistoryArray.length; i++) {
                musicHistory.add(musicHistoryArray[i]);
            }
            musicHistoryCurrent = savedInstanceState.getInt("music_history_current");
        } else {
            musicHistoryCurrent = 0;
        }

        musicPlayer = (MusicPlayer)findViewById(R.id.main_music_music_player);

        musicPlayer.setOnInfoClickListener(() -> {
            scrollToMusicByPosition(musicAdapter.getSelectedPosition());
        });

        musicPlayer.setOnPreviousListener((boolean inHistory) -> {
            if (inHistory || isShuffling) {
                if (musicHistoryCurrent > 0) {
                    musicHistoryCurrent--;
                    for (Music musicItem : music) {
                        if (musicItem.getId() == musicHistory.get(musicHistoryCurrent)) {
                            playMusicByPosition(musicAdapter.getPosition(musicItem), 0, true, true);
                            return;
                        }
                    }
                } else if (isShuffling) {
                    playMusicByPosition((int)(Math.random() * musicAdapter.getCount()));
                }
            } else {
                playMusicByPosition(musicAdapter.getSelectedPosition() == 0 ? musicAdapter.getCount() - 1 : musicAdapter.getSelectedPosition() - 1);
            }
        });

        musicPlayer.setOnNextListener((boolean inHistory) -> {
            if (inHistory || isShuffling) {
                if (musicHistoryCurrent < musicHistory.size() - 1) {
                    musicHistoryCurrent++;
                    for (Music musicItem : music) {
                        if (musicItem.getId() == musicHistory.get(musicHistoryCurrent)) {
                            playMusicByPosition(musicAdapter.getPosition(musicItem), 0, true, true);
                            return;
                        }
                    }
                } else if (isShuffling) {
                    playMusicByPosition((int)(Math.random() * musicAdapter.getCount()));
                }
            } else {
                playMusicByPosition(musicAdapter.getSelectedPosition() == musicAdapter.getCount() - 1 ? 0 : musicAdapter.getSelectedPosition() + 1);
            }
        });

        musicList = (ListView)findViewById(R.id.main_music_list);
        musicList.setFastScrollEnabled(settings.getBoolean("fast_scroll", Config.SETTINGS_FAST_SCROLL_DEFAULT));

        musicAdapter = new MusicAdapter(this);
        musicList.setAdapter(musicAdapter);

        musicList.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long id) -> {
            playMusicByPosition(position);
        });

        ImageButton musicShuffleButton = (ImageButton)findViewById(R.id.main_music_shuffle_button);
        isShuffling = settings.getBoolean("shuffling", false);
        if (isShuffling) {
            musicShuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
        }
        musicShuffleButton.setOnClickListener((View view) -> {
            playMusicByPosition((int)(Math.random() * musicAdapter.getCount()));
        });
        musicShuffleButton.setOnLongClickListener((View view) -> {
            isShuffling = !isShuffling;
            if (isShuffling) {
                musicShuffleButton.setImageResource(R.drawable.ic_shuffle_disabled);
            } else {
                musicShuffleButton.setImageResource(R.drawable.ic_shuffle);
            }
            rememberShuffling();
            return true;
        });

        ((ImageButton)findViewById(R.id.main_music_refresh_button)).setOnClickListener((View view) -> {
            boolean isPlaying = musicPlayer.isPlaying();
            if (isPlaying) {
                musicPlayer.pause();
            }
            rememberMusic();
            loadMusic(isPlaying);
        });

        ((ImageButton)findViewById(R.id.main_music_search_button)).setOnClickListener((View view) -> {
            startActivityForResult(new Intent(this, SearchActivity.class), MainActivity.SEARCH_ACTIVITY_REQUEST_CODE);
        });

        ((ImageButton)findViewById(R.id.main_music_settings_button)).setOnClickListener((View view) -> {
            oldLanguage = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_ACTIVITY_REQUEST_CODE);
        });

        // Empty page
        View.OnClickListener refreshEmptyOnClick = (View view) -> {
            musicPage.setVisibility(View.VISIBLE);
            emptyPage.setVisibility(View.GONE);
            loadMusic(false);
        };
        ((ImageButton)findViewById(R.id.main_empty_refresh_button)).setOnClickListener(refreshEmptyOnClick);
        ((Button)findViewById(R.id.main_empty_hero_button)).setOnClickListener(refreshEmptyOnClick);

        // Access page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View.OnClickListener accessOnClick = (View view) -> {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, MainActivity.STORAGE_PERMISSION_REQUEST_CODE);
            };
            ((ImageButton)findViewById(R.id.main_access_refresh_button)).setOnClickListener(accessOnClick);
            ((Button)findViewById(R.id.main_access_hero_button)).setOnClickListener(accessOnClick);

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                accessPage.setVisibility(View.VISIBLE);
                musicPage.setVisibility(View.GONE);
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, MainActivity.STORAGE_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean("is_music_playing")) {
            loadMusic(true);
        } else {
            loadMusic(false);
        }

        RatingAlert.updateAndShow(this);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        long[] musicHistoryArray = new long[musicHistory.size()];
        for (int i = 0; i < musicHistory.size(); i++) {
            musicHistoryArray[i] = musicHistory.get(i);
        }
        savedInstanceState.putLongArray("music_history", musicHistoryArray);
        savedInstanceState.putInt("music_history_current", musicHistoryCurrent);
        savedInstanceState.putBoolean("is_music_playing", musicPlayer.isPlaying());
    }

    public void onBackPressed() {
        moveTaskToBack(false);
    }

    public void onPause() {
        if (settings.getBoolean("remember_music", Config.SETTINGS_REMEMBER_MUSIC_DEFAULT)) {
            rememberMusic();
        }
        super.onPause();
    }

    public void onDestroy() {
        musicPlayer.release();
        super.onDestroy();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MainActivity.STORAGE_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            musicPage.setVisibility(View.VISIBLE);
            accessPage.setVisibility(View.GONE);
            loadMusic(false);
            RatingAlert.updateAndShow(this);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SEARCH_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            long musicId = data.getLongExtra("id", -1);
            if (musicId != -1) {
                for (Music musicItem : music) {
                    if (musicItem.getId() == musicId) {
                        handler.post(() -> {
                            playMusicByPosition(musicAdapter.getPosition(musicItem));
                        });
                        return;
                    }
                }
            }
        }

        if (requestCode == MainActivity.SETTINGS_ACTIVITY_REQUEST_CODE) {
            // Check if language or theme settings have changed
            if (
                oldLanguage != settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT) ||
                oldTheme != settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT)
            ) {
                handler.post(() -> {
                    recreate();
                });
            }

            // Else update fast scroll music list setting
            else {
                musicList.setFastScrollEnabled(settings.getBoolean("fast_scroll", Config.SETTINGS_FAST_SCROLL_DEFAULT));
            }
        }
    }

    private void rememberShuffling() {
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("shuffling", isShuffling);
        settingsEditor.apply();
    }

    private void rememberMusic() {
        if (musicAdapter.getSelectedPosition() != -1) {
            SharedPreferences.Editor settingsEditor = settings.edit();
            Music music = musicAdapter.getItem(musicAdapter.getSelectedPosition());
            settingsEditor.putLong("playing_music_id", music.getId());
            settingsEditor.putInt("playing_music_position", musicPlayer.getCurrentPosition());
            settingsEditor.apply();
        }
    }

    private void loadMusic(boolean isAutoPlayed) {
        music = Music.loadMusic(this);
        musicAdapter.clear();
        musicAdapter.addAll(music);

        if (music.size() == 0) {
            musicPage.setVisibility(View.GONE);
            emptyPage.setVisibility(View.VISIBLE);
        } else {
            long musicId = settings.getLong("playing_music_id", -1);
            boolean isMusicFound = false;
            if (musicId != -1) {
                for (Music musicItem : music) {
                    if (musicItem.getId() == musicId) {
                        isMusicFound = true;
                        playMusicByPosition(musicAdapter.getPosition(musicItem), settings.getInt("playing_music_position", 0), isAutoPlayed, false);
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
        musicAdapter.setSelectedPosition(position);
        scrollToMusicByPosition(position);

        Music music = musicAdapter.getItem(position);
        if (musicHistoryCurrent == musicHistory.size() - 1 && musicHistory.get(musicHistory.size() - 1) == music.getId()) {
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
                musicList.setSelection(position - (musicList.getLastVisiblePosition() - musicList.getFirstVisiblePosition() - 1));
            }
        });
    }
}
