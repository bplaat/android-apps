package nl.plaatsoft.redsquare.android;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GamePage gamePage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout menuPage = (RelativeLayout)findViewById(R.id.menu_page);
        gamePage = (GamePage)findViewById(R.id.game_page);
        LinearLayout gameoverPage = (LinearLayout)findViewById(R.id.gameover_page);

        // Menu page
        try {
            ((TextView)findViewById(R.id.menu_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        ((Button)findViewById(R.id.menu_play_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                menuPage.setVisibility(View.GONE);
                gamePage.setVisibility(View.VISIBLE);

                gamePage.start();
            }
        });
        ((Button)findViewById(R.id.menu_exit_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        // Game page
        TextView gameoverScoreLabel = (TextView)findViewById(R.id.gameover_score_label);
        String scoreLabelString = getResources().getString(R.string.score_label);
        TextView gameoverTimeLabel = (TextView)findViewById(R.id.gameover_time_label);
        String timeLabelString = getResources().getString(R.string.time_label);
        TextView gameoverLevelLabel = (TextView)findViewById(R.id.gameover_level_label);
        String levelLabelString = getResources().getString(R.string.level_label);
        gamePage.setOnEventListener(new GamePage.OnEventListener() {
            public void onGameover(int score, int seconds, int level) {
                gameoverScoreLabel.setText(String.format(scoreLabelString, score));

                gameoverTimeLabel.setText(String.format(timeLabelString, seconds / 60, seconds % 60));

                gameoverLevelLabel.setText(String.format(levelLabelString, level));

                gameoverPage.setVisibility(View.VISIBLE);
            }
        });

        // Game over page
        ((Button)findViewById(R.id.gameover_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gamePage.setVisibility(View.GONE);
                gameoverPage.setVisibility(View.GONE);
                menuPage.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN
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
