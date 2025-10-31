package nl.plaatsoft.nos.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends BaseActivity {
    public static final int LANGUAGE_DEFAULT = 2;
    public static final int THEME_DEFAULT = 2;

    private LinearLayout[] tabs;
    private LinearLayout[] buttons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // News tabs
        initNewsTab((ListView)findViewById(R.id.latest_articles_list), "http://feeds.nos.nl/nosnieuwsalgemeen", (ImageView)findViewById(R.id.latest_refresh_button));
        initNewsTab((ListView)findViewById(R.id.sports_articles_list), "http://feeds.nos.nl/nossportalgemeen", (ImageView)findViewById(R.id.sports_refresh_button));
        initNewsTab((ListView)findViewById(R.id.economy_articles_list), "http://feeds.nos.nl/nosnieuwseconomie", (ImageView)findViewById(R.id.economy_refresh_button));
        initNewsTab((ListView)findViewById(R.id.tech_articles_list), "http://feeds.nos.nl/nosnieuwstech", (ImageView)findViewById(R.id.tech_refresh_button));

        // Settings tab
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

        ((TextView)findViewById(R.id.settings_about_button)).setOnClickListener((View view) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bplaat.nl/")));
        });

        // Bottom bar
        tabs = new LinearLayout[5];
        tabs[0] = (LinearLayout)findViewById(R.id.latest_tab);
        tabs[1] = (LinearLayout)findViewById(R.id.sports_tab);
        tabs[2] = (LinearLayout)findViewById(R.id.economy_tab);
        tabs[3] = (LinearLayout)findViewById(R.id.tech_tab);
        tabs[4] = (LinearLayout)findViewById(R.id.settings_tab);

        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setTag(i);
        }

        buttons = new LinearLayout[5];
        buttons[0] = (LinearLayout)findViewById(R.id.latest_button);
        buttons[1] = (LinearLayout)findViewById(R.id.sports_button);
        buttons[2] = (LinearLayout)findViewById(R.id.economy_button);
        buttons[3] = (LinearLayout)findViewById(R.id.tech_button);
        buttons[4] = (LinearLayout)findViewById(R.id.settings_button);

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setTag(i);
            buttons[i].setOnClickListener((View view) -> {
                openTab((int)view.getTag(), true);
            });
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        openTab(savedInstanceState.getInt("openTab"), false);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        for (LinearLayout tab : tabs) {
            if (tab.getVisibility() == View.VISIBLE) {
                savedInstanceState.putInt("openTab", (int)tab.getTag());
                break;
            }
        }
    }

    public void onBackPressed() {
        if (tabs[0].getVisibility() != View.VISIBLE) {
            openTab(0, true);
        }
        else {
            super.onBackPressed();
        }
    }

    private void openTab(int index, boolean withAnimation) {
        for (LinearLayout tab : tabs) {
            if ((int)tab.getTag() == index) {
                if (tab.getVisibility() != View.VISIBLE) {
                    tab.setVisibility(View.VISIBLE);

                    if (withAnimation) {
                        View tabContent = tab.getChildAt(1);
                        tabContent.setAlpha(0f);
                        tabContent.setScaleX(0.98f);
                        tabContent.setScaleY(0.98f);
                        tabContent.animate().alpha(1).scaleX(1).scaleY(1).setDuration(150);
                    }
                }
            } else {
                tab.setVisibility(View.GONE);
            }
        }

        for (LinearLayout button : buttons) {
            if ((int)button.getTag() == index) {
                button.animate().alpha(1f).setDuration(150);
            } else {
                TypedValue buttonInactiveAlphaValue = new TypedValue();
                getResources().getValue(R.dimen.bottom_bar_button_inactive_alpha, buttonInactiveAlphaValue, true);
                button.animate().alpha(buttonInactiveAlphaValue.getFloat()).setDuration(150);
            }
        }
    }

    private void initNewsTab(ListView listView, String rssUrl, ImageView refreshButton) {
        ArticlesAdapter articlesAdapter = new ArticlesAdapter(this);
        listView.setAdapter(articlesAdapter);
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(this, ArticleActivity.class);
            intent.putExtra("article", articlesAdapter.getItem(position));
            startActivity(intent);
        });

        FetchDataTask fetchDataTask = null;
        refreshButton.setOnClickListener((View view) -> {
            if (fetchDataTask != null && !fetchDataTask.isFinished()) {
                fetchDataTask.cancel();
            }
            articlesAdapter.clear();
            fetchNewsData(fetchDataTask, articlesAdapter, rssUrl, false);
        });

        fetchNewsData(fetchDataTask, articlesAdapter, rssUrl, true);
    }

    private void fetchNewsData(FetchDataTask fetchDataTask, ArticlesAdapter articlesAdapter, String rssUrl, boolean loadFromCache) {
        try {
            fetchDataTask = new FetchDataTask(this, "https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(rssUrl, "UTF-8") + "&api_key=" + Config.API_KEY + "&count=20", loadFromCache, true, (String data) -> {
                try {
                    JSONObject feed = new JSONObject(data);
                    JSONArray articles = feed.getJSONArray("items");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        articlesAdapter.add(new Article(
                            article.getString("title"),
                            article.getJSONObject("enclosure").getString("link"),
                            article.getString("pubDate"),
                            article.getString("content")
                        ));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
