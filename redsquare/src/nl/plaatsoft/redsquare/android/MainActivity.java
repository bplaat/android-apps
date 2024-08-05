package nl.plaatsoft.redsquare.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

public class MainActivity extends BaseActivity {
    public static final String SCORE_DEFAULT = "[]";
    public static final String NAME_DEFAULT = "Anonymous";
    public static final int LANGUAGE_DEFAULT = 2;
    public static final int THEME_DEFAULT = 2;

    private GamePage gamePage;
    private RelativeLayout menuPage;
    private LinearLayout settingsPage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
            getWindow().setAttributes(attributes);
        }
        setContentView(R.layout.activity_main);

        menuPage = (RelativeLayout)findViewById(R.id.menu_page);
        gamePage = (GamePage)findViewById(R.id.game_page);
        LinearLayout gameoverPage = (LinearLayout)findViewById(R.id.gameover_page);
        LinearLayout localHighscorePage = (LinearLayout)findViewById(R.id.local_highscore_page);
        LinearLayout globalHighscorePage = (LinearLayout)findViewById(R.id.global_highscore_page);
        LinearLayout helpPage = (LinearLayout)findViewById(R.id.help_page);
        settingsPage = (LinearLayout)findViewById(R.id.settings_page);

        // Menu page
        try {
            ((TextView)findViewById(R.id.menu_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        ((TextView)findViewById(R.id.menu_about_label)).setOnClickListener((View view) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bplaat.nl/")));
        });

        // Game page
        ((Button)findViewById(R.id.menu_play_button)).setOnClickListener((View view) -> {
            menuPage.setVisibility(View.GONE);
            gamePage.setVisibility(View.VISIBLE);

            gamePage.start();
        });

        // Game Over page
        TextView gameoverScoreLabel = (TextView)findViewById(R.id.gameover_score_label);
        String scoreLabelString = getResources().getString(R.string.gameover_score_label);
        TextView gameoverTimeLabel = (TextView)findViewById(R.id.gameover_time_label);
        String timeLabelString = getResources().getString(R.string.gameover_time_label);
        TextView gameoverLevelLabel = (TextView)findViewById(R.id.gameover_level_label);
        String levelLabelString = getResources().getString(R.string.gameover_level_label);

        gamePage.setOnEventListener((int score, int seconds, int level) -> {
            try {
                new FetchDataTask(MainActivity.this, Config.API_URL + "?key=" + Config.API_KEY + "&name=" + URLEncoder.encode(settings.getString("name", MainActivity.NAME_DEFAULT), "UTF-8") + "&score=" + score, false, false, null);

                JSONArray scoresJSON = new JSONArray(settings.getString("scores", MainActivity.SCORE_DEFAULT));

                JSONObject newScoreJSON = new JSONObject();
                newScoreJSON.put("name", settings.getString("name", MainActivity.NAME_DEFAULT));
                newScoreJSON.put("score", score);
                scoresJSON.put(newScoreJSON);

                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putString("scores", scoresJSON.toString());
                settingsEditor.apply();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            gameoverScoreLabel.setText(String.format(scoreLabelString, score));

            gameoverTimeLabel.setText(String.format(timeLabelString, seconds / 60, seconds % 60));

            gameoverLevelLabel.setText(String.format(levelLabelString, level));

            gameoverPage.setVisibility(View.VISIBLE);
        });

        ((Button)findViewById(R.id.gameover_back_button)).setOnClickListener((View view) -> {
            gamePage.setVisibility(View.GONE);
            gameoverPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Local High Score page
        ScoreAdapter localHighscoreAdapter = new ScoreAdapter(this);
        ListView localHighscoreList = (ListView)findViewById(R.id.local_highscore_list);
        localHighscoreList.setAdapter(localHighscoreAdapter);

        ((Button)findViewById(R.id.menu_local_highscore_button)).setOnClickListener((View view) -> {
            try {
                localHighscoreAdapter.clear();
                JSONArray scoresJSON = new JSONArray(settings.getString("scores", MainActivity.SCORE_DEFAULT));
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
                exception.printStackTrace();
            }

            menuPage.setVisibility(View.GONE);
            localHighscorePage.setVisibility(View.VISIBLE);
        });

        ((Button)findViewById(R.id.local_highscore_back_button)).setOnClickListener((View view) -> {
            localHighscorePage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Global High Score page
        ScoreAdapter globalHighscoreAdapter = new ScoreAdapter(this);
        ListView globalHighscoreList = (ListView)findViewById(R.id.global_highscore_list);
        globalHighscoreList.setAdapter(globalHighscoreAdapter);
        TextView globalHighscoreListLoading = (TextView)findViewById(R.id.global_highscore_list_loading);
        TextView globalHighscoreListError = (TextView)findViewById(R.id.global_highscore_list_error);

        ((Button)findViewById(R.id.menu_global_highscore_button)).setOnClickListener((View view) -> {
            globalHighscoreAdapter.clear();
            globalHighscoreList.setVisibility(View.GONE);
            globalHighscoreListLoading.setVisibility(View.VISIBLE);
            globalHighscoreListError.setVisibility(View.GONE);

            new FetchDataTask(MainActivity.this, Config.API_URL + "?key=" + Config.API_KEY + "&page=1&limit=50", false, false, (String data) -> {
                if (data != null) {
                    try {
                        JSONObject dataJSON = new JSONObject(data);
                        JSONArray scoresJSON = dataJSON.getJSONArray("scores");
                        for (int i = 0; i < scoresJSON.length(); i++) {
                            JSONObject scoreJSON = scoresJSON.getJSONObject(i);
                            globalHighscoreAdapter.add(new Score(
                                scoreJSON.getString("name"),
                                scoreJSON.getInt("score")
                            ));
                        }

                        globalHighscoreList.setVisibility(View.VISIBLE);
                        globalHighscoreListLoading.setVisibility(View.GONE);
                        globalHighscoreListError.setVisibility(View.GONE);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    globalHighscoreList.setVisibility(View.GONE);
                    globalHighscoreListLoading.setVisibility(View.GONE);
                    globalHighscoreListError.setVisibility(View.VISIBLE);
                }
            });

            menuPage.setVisibility(View.GONE);
            globalHighscorePage.setVisibility(View.VISIBLE);
        });

        ((Button)findViewById(R.id.global_highscore_back_button)).setOnClickListener((View view) -> {
            globalHighscorePage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Help page
        ((Button)findViewById(R.id.menu_help_button)).setOnClickListener((View view) -> {
            menuPage.setVisibility(View.GONE);
            helpPage.setVisibility(View.VISIBLE);
        });

        ((Button)findViewById(R.id.help_back_button)).setOnClickListener((View view) -> {
            helpPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });

        // Settings page
        EditText settingsNameInput = (EditText)findViewById(R.id.settings_name_input);
        settingsNameInput.setText(settings.getString("name", MainActivity.NAME_DEFAULT));
        settingsNameInput.setSelection(settingsNameInput.getText().length());

        settingsNameInput.setOnEditorActionListener((TextView view, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putString("name", settingsNameInput.getText().toString());
                settingsEditor.apply();

                hideSystemUI();
            }
            return false;
        });

        ((Button)findViewById(R.id.menu_settings_button)).setOnClickListener((View view) -> {
            settingsNameInput.setText(settings.getString("name", MainActivity.NAME_DEFAULT));
            settingsNameInput.setSelection(settingsNameInput.getText().length());

            menuPage.setVisibility(View.GONE);
            settingsPage.setVisibility(View.VISIBLE);
        });

        String[] languages = getResources().getStringArray(R.array.languages);
        int language = settings.getInt("language", MainActivity.LANGUAGE_DEFAULT);
        ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);

        ((LinearLayout)findViewById(R.id.settings_language_button)).setOnClickListener((View view) -> {
            new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.settings_language))
                .setSingleChoiceItems(languages, language, (DialogInterface dialog, int which) -> {
                    SharedPreferences.Editor settingsEditor = settings.edit();
                    settingsEditor.putInt("language", which);
                    settingsEditor.apply();

                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(getResources().getString(R.string.settings_cancel), null)
                .show();
        });

        String[] themes = getResources().getStringArray(R.array.themes);
        int theme = settings.getInt("theme", MainActivity.THEME_DEFAULT);
        ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);

        ((LinearLayout)findViewById(R.id.settings_theme_button)).setOnClickListener((View view) -> {
            new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.settings_theme))
                .setSingleChoiceItems(themes, theme, (DialogInterface dialog, int which) -> {
                    SharedPreferences.Editor settingsEditor = settings.edit();
                    settingsEditor.putInt("theme", which);
                    settingsEditor.apply();

                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(getResources().getString(R.string.settings_cancel), null)
                .show();
        });

        ((Button)findViewById(R.id.settings_back_button)).setOnClickListener((View view) -> {
            settingsPage.setVisibility(View.GONE);
            menuPage.setVisibility(View.VISIBLE);
        });
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getBoolean("settingsPageOpen", false)) {
            menuPage.setVisibility(View.GONE);
            settingsPage.setVisibility(View.VISIBLE);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("settingsPageOpen", settingsPage.getVisibility() == View.VISIBLE);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
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

    public void onResume() {
        super.onResume();
        if (gamePage.getVisibility() == View.VISIBLE) {
            gamePage.start();
        }
    }

    public void onPause() {
        if (gamePage.isRunning()) {
            gamePage.stop();
        }
        super.onPause();
    }
}
