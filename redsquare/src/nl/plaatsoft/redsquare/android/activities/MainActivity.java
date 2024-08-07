package nl.plaatsoft.redsquare.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.WindowInsetsController;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.net.URLEncoder;
import java.util.Comparator;
import org.json.JSONArray;
import org.json.JSONObject;

import nl.plaatsoft.redsquare.android.components.GamePage;
import nl.plaatsoft.redsquare.android.components.ScoreAdapter;
import nl.plaatsoft.redsquare.android.models.Score;
import nl.plaatsoft.redsquare.android.tasks.FetchDataTask;
import nl.plaatsoft.redsquare.android.Config;
import nl.plaatsoft.redsquare.android.Consts;
import nl.plaatsoft.redsquare.android.Utils;
import nl.plaatsoft.redsquare.android.R;

public class MainActivity extends BaseActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private GamePage gamePage;
    private RelativeLayout menuPage;
    private LinearLayout settingsPage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Utils.windowSetDecorFitsSystemWindows(getWindow(), false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var attributes = getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            getWindow().setAttributes(attributes);
        }
        setContentView(R.layout.activity_main);

        menuPage = findViewById(R.id.menu_page);
        gamePage = findViewById(R.id.game_page);
        var gameoverPage = (LinearLayout)findViewById(R.id.gameover_page);
        var localHighscorePage = (LinearLayout)findViewById(R.id.local_highscore_page);
        var globalHighscorePage = (LinearLayout)findViewById(R.id.global_highscore_page);
        var helpPage = findViewById(R.id.help_page);
        settingsPage = findViewById(R.id.settings_page);

        // Menu page
        try {
            ((TextView)findViewById(R.id.menu_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            Log.e(getPackageName(), "Can't get app version", exception);
        }
        findViewById(R.id.menu_about_label).setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bplaat.nl/")));
        });

        // Game page
        findViewById(R.id.menu_play_button).setOnClickListener(view -> {
            menuPage.setVisibility(View.GONE);
            gamePage.setVisibility(View.VISIBLE);
            handler.post(() -> gamePage.start());
        });

        // Game Over page
        var gameoverScoreLabel = (TextView)findViewById(R.id.gameover_score_label);
        var scoreLabelString = getResources().getString(R.string.gameover_score_label);
        var gameoverTimeLabel = (TextView)findViewById(R.id.gameover_time_label);
        var timeLabelString = getResources().getString(R.string.gameover_time_label);
        var gameoverLevelLabel = (TextView)findViewById(R.id.gameover_level_label);
        var levelLabelString = getResources().getString(R.string.gameover_level_label);

        gamePage.setOnEventListener((int score, int seconds, int level) -> {
            try {
                FetchDataTask.with(this).load(Config.API_URL + "?key=" + Config.API_KEY + "&name=" + URLEncoder.encode(settings.getString("name", Consts.Settings.NAME_DEFAULT), "UTF-8") + "&score=" + score).fetch();

                var scoresJSON = new JSONArray(settings.getString("scores", Consts.Settings.SCORE_DEFAULT));

                var newScoreJSON = new JSONObject();
                newScoreJSON.put("name", settings.getString("name", Consts.Settings.NAME_DEFAULT));
                newScoreJSON.put("score", score);
                scoresJSON.put(newScoreJSON);

                var settingsEditor = settings.edit();
                settingsEditor.putString("scores", scoresJSON.toString());
                settingsEditor.apply();
            } catch (Exception exception) {
                Log.e(getPackageName(), "Can't parse local highscores", exception);
            }

            gameoverScoreLabel.setText(String.format(scoreLabelString, score));
            gameoverTimeLabel.setText(String.format(timeLabelString, seconds / 60, seconds % 60));
            gameoverLevelLabel.setText(String.format(levelLabelString, level));
            gameoverPage.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.gameover_back_button).setOnClickListener(view -> {
            gamePage.setVisibility(View.GONE);
            gameoverPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Local High Score page
        var localHighscoreAdapter = new ScoreAdapter(this);
        var localHighscoreList = (ListView)findViewById(R.id.local_highscore_list);
        localHighscoreList.setAdapter(localHighscoreAdapter);

        findViewById(R.id.menu_local_highscore_button).setOnClickListener(view -> {
            try {
                localHighscoreAdapter.clear();
                JSONArray scoresJSON = new JSONArray(settings.getString("scores", Consts.Settings.SCORE_DEFAULT));
                for (int i = 0; i < scoresJSON.length(); i++) {
                    JSONObject scoreJSON = scoresJSON.getJSONObject(i);
                    localHighscoreAdapter.add(new Score(
                        scoreJSON.getString("name"),
                        scoreJSON.getInt("score")
                    ));
                }

                localHighscoreAdapter.sort((Score a, Score b) -> b.getScore() - a.getScore());
                localHighscoreAdapter.notifyDataSetChanged();
            } catch (Exception exception) {
                Log.e(getPackageName(), "Can't parse local highscores", exception);
            }

            menuPage.setVisibility(View.GONE);
            localHighscorePage.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.local_highscore_back_button).setOnClickListener(view -> {
            localHighscorePage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Global High Score page
        var globalHighscoreAdapter = new ScoreAdapter(this);
        var globalHighscoreList = (ListView)findViewById(R.id.global_highscore_list);
        globalHighscoreList.setAdapter(globalHighscoreAdapter);
        var globalHighscoreListLoading = (TextView)findViewById(R.id.global_highscore_list_loading);
        var globalHighscoreListError = (TextView)findViewById(R.id.global_highscore_list_error);

        findViewById(R.id.menu_global_highscore_button).setOnClickListener(view -> {
            globalHighscoreAdapter.clear();
            globalHighscoreList.setVisibility(View.GONE);
            globalHighscoreListLoading.setVisibility(View.VISIBLE);
            globalHighscoreListError.setVisibility(View.GONE);

            FetchDataTask.with(this).load(Config.API_URL + "?key=" + Config.API_KEY + "&page=1&limit=50").then(data -> {
                try {
                    var dataJSON = new JSONObject(new String(data, "UTF-8"));
                    var scoresJSON = dataJSON.getJSONArray("scores");
                    for (int i = 0; i < scoresJSON.length(); i++) {
                        var scoreJSON = scoresJSON.getJSONObject(i);
                        globalHighscoreAdapter.add(new Score(
                            scoreJSON.getString("name"),
                            scoreJSON.getInt("score")
                        ));
                    }

                    globalHighscoreList.setVisibility(View.VISIBLE);
                    globalHighscoreListLoading.setVisibility(View.GONE);
                    globalHighscoreListError.setVisibility(View.GONE);
                } catch (Exception exception) {
                    Log.e(getPackageName(), "Can't parse global highscores", exception);
                }
            }, exception -> {
                Log.e(getPackageName(), "Can't fetch global highscores", exception);
                globalHighscoreList.setVisibility(View.GONE);
                globalHighscoreListLoading.setVisibility(View.GONE);
                globalHighscoreListError.setVisibility(View.VISIBLE);
            }).fetch();

            menuPage.setVisibility(View.GONE);
            globalHighscorePage.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.global_highscore_back_button).setOnClickListener(view -> {
            globalHighscorePage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Help page
        findViewById(R.id.menu_help_button).setOnClickListener(view -> {
            menuPage.setVisibility(View.GONE);
            helpPage.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.help_back_button).setOnClickListener(view -> {
            helpPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Settings page
        findViewById(R.id.menu_settings_button).setOnClickListener(view -> {
            menuPage.setVisibility(View.GONE);
            settingsPage.setVisibility(View.VISIBLE);
        });

        // Name input
        var settingsNameInput = (EditText)findViewById(R.id.settings_name_input);
        settingsNameInput.setText(settings.getString("name", Consts.Settings.NAME_DEFAULT));
        settingsNameInput.setSelection(settingsNameInput.getText().length());
        settingsNameInput.setOnEditorActionListener((TextView view, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                var settingsEditor = settings.edit();
                settingsEditor.putString("name", settingsNameInput.getText().toString());
                settingsEditor.apply();
                hideSystemUI();
            }
            return false;
        });

        // Language button
        var languages = new String[] {
            getResources().getString(R.string.settings_language_english),
            getResources().getString(R.string.settings_language_dutch),
            getResources().getString(R.string.settings_language_system)
        };
        var language = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
        ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);
        findViewById(R.id.settings_language_button).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language_alert_title_label)
                .setSingleChoiceItems(languages, language, (DialogInterface dialog, int which) -> {
                    var settingsEditor = settings.edit();
                    settingsEditor.putInt("language", which);
                    settingsEditor.apply();
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(R.string.settings_language_alert_cancel_button, null)
                .show();
        });

        // Theme button
        var themes = new String[] {
            getResources().getString(R.string.settings_theme_light),
            getResources().getString(R.string.settings_theme_dark),
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                ? getResources().getString(R.string.settings_theme_battery_saver)
                : getResources().getString(R.string.settings_theme_system)
        };
        var theme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
        ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);
        findViewById(R.id.settings_theme_button).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme_alert_title_label)
                .setSingleChoiceItems(themes, theme, (DialogInterface dialog, int which) -> {
                    var settingsEditor = settings.edit();
                    settingsEditor.putInt("theme", which);
                    settingsEditor.apply();
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(R.string.settings_theme_alert_cancel_button, null)
                .show();
        });

        findViewById(R.id.settings_back_button).setOnClickListener(view -> {
            settingsPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("settingsPageOpen", settingsPage.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean("settingsPageOpen", false)) {
            menuPage.setVisibility(View.GONE);
            settingsPage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            hideSystemUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gamePage.getVisibility() == View.VISIBLE)
            gamePage.start();
    }

    @Override
    public void onPause() {
        if (gamePage.isRunning())
            gamePage.stop();
        super.onPause();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.hide(WindowInsets.Type.systemBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }
}
