package nl.plaatsoft.redsquare.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
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

public class MainActivity extends Activity {
    private GamePage gamePage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        RelativeLayout menuPage = (RelativeLayout)findViewById(R.id.menu_page);
        gamePage = (GamePage)findViewById(R.id.game_page);
        LinearLayout gameoverPage = (LinearLayout)findViewById(R.id.gameover_page);
        LinearLayout localHighscorePage = (LinearLayout)findViewById(R.id.local_highscore_page);
        LinearLayout globalHighscorePage = (LinearLayout)findViewById(R.id.global_highscore_page);
        LinearLayout helpPage = (LinearLayout)findViewById(R.id.help_page);
        LinearLayout settingsPage = (LinearLayout)findViewById(R.id.settings_page);

        // Menu page
        try {
            ((TextView)findViewById(R.id.menu_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Game page
        ((Button)findViewById(R.id.menu_play_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                menuPage.setVisibility(View.GONE);
                gamePage.setVisibility(View.VISIBLE);

                gamePage.start();
            }
        });

        // Game Over page
        TextView gameoverScoreLabel = (TextView)findViewById(R.id.gameover_score_label);
        String scoreLabelString = getResources().getString(R.string.score_label);
        TextView gameoverTimeLabel = (TextView)findViewById(R.id.gameover_time_label);
        String timeLabelString = getResources().getString(R.string.time_label);
        TextView gameoverLevelLabel = (TextView)findViewById(R.id.gameover_level_label);
        String levelLabelString = getResources().getString(R.string.level_label);

        gamePage.setOnEventListener(new GamePage.OnEventListener() {
            public void onGameover(int score, int seconds, int level) {
                try {
                    new FetchDataTask(Config.API_URL + "?key=" + Config.API_KEY + "&name=" + URLEncoder.encode(preferences.getString("name", "Anonymous"), "UTF-8") + "&score=" + score, null).execute();

                    JSONArray scoresJSON = new JSONArray(preferences.getString("scores", "[]"));

                    JSONObject newScoreJSON = new JSONObject();
                    newScoreJSON.put("name", preferences.getString("name", "Anonymous"));
                    newScoreJSON.put("score", score);
                    scoresJSON.put(newScoreJSON);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("scores", scoresJSON.toString());
                    editor.apply();

                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                gameoverScoreLabel.setText(String.format(scoreLabelString, score));

                gameoverTimeLabel.setText(String.format(timeLabelString, seconds / 60, seconds % 60));

                gameoverLevelLabel.setText(String.format(levelLabelString, level));

                gameoverPage.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.gameover_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gamePage.setVisibility(View.GONE);
                gameoverPage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });

        // Local High Score page
        ScoreAdapter localHighscoreAdapter = new ScoreAdapter(this);
        ListView localHighscoreList = (ListView)findViewById(R.id.local_highscore_list);
        localHighscoreList.setAdapter(localHighscoreAdapter);

        ((Button)findViewById(R.id.menu_local_highscore_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    localHighscoreAdapter.clear();
                    JSONArray scoresJSON = new JSONArray(preferences.getString("scores", "[]"));
                    for (int i = 0; i < scoresJSON.length(); i++) {
                        JSONObject scoreJSON = scoresJSON.getJSONObject(i);
                        localHighscoreAdapter.add(new Score(
                            scoreJSON.getString("name"),
                            scoreJSON.getInt("score")
                        ));
                    }

                    localHighscoreAdapter.sort(new Comparator<Score>() {
                        public int compare(Score a, Score b) {
                            return b.getScore() - a.getScore();
                        }
                    });
                    localHighscoreAdapter.notifyDataSetChanged();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                menuPage.setVisibility(View.GONE);
                localHighscorePage.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.local_highscore_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                localHighscorePage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });

        // Global High Score page
        ScoreAdapter globalHighscoreAdapter = new ScoreAdapter(this);
        ListView globalHighscoreList = (ListView)findViewById(R.id.global_highscore_list);
        globalHighscoreList.setAdapter(globalHighscoreAdapter);
        TextView globalHighscoreListLoading = (TextView)findViewById(R.id.global_highscore_list_loading);
        TextView globalHighscoreListError = (TextView)findViewById(R.id.global_highscore_list_error);

        ((Button)findViewById(R.id.menu_global_highscore_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                globalHighscoreAdapter.clear();
                globalHighscoreList.setVisibility(View.GONE);
                globalHighscoreListLoading.setVisibility(View.VISIBLE);
                globalHighscoreListError.setVisibility(View.GONE);

                new FetchDataTask(Config.API_URL + "?key=" + Config.API_KEY + "&page=1&limit=50", new FetchDataTask.OnLoadListener() {
                    public void onLoad(String data) {
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
                    }
                }).execute();

                menuPage.setVisibility(View.GONE);
                globalHighscorePage.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.global_highscore_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                globalHighscorePage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });

        // Help page
        ((Button)findViewById(R.id.menu_help_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                menuPage.setVisibility(View.GONE);
                helpPage.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.help_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                helpPage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });

        // Settings page
        EditText settingsNameInput = (EditText)findViewById(R.id.settings_name_input);
        settingsNameInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSystemUI();
                }
                return false;
            }
        });

        ((Button)findViewById(R.id.menu_settings_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                settingsNameInput.setText(preferences.getString("name", "Anonymous"));
                settingsNameInput.setSelection(settingsNameInput.getText().length());

                menuPage.setVisibility(View.GONE);
                settingsPage.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.settings_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("name", settingsNameInput.getText().toString());
                editor.apply();

                settingsPage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN
        );
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

    public void onBackPressed() {
        moveTaskToBack(false);
    }
}
