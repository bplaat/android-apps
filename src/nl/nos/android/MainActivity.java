package nl.nos.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

public class MainActivity extends Activity {
    private static final String API_KEY = "s3khkckng9or74lykjvhufbetd8jgtxcf265ltrh";

    private LinearLayout mainPage;
    private LinearLayout articlePage;
    private LinearLayout[] tabs;
    private LinearLayout[] buttons;

    private void openArticlePage(Article article) {
        FetchImageTask.fetchImage(this, (ImageView)findViewById(R.id.article_image), article.getImageUrl());
        ((TextView)findViewById(R.id.article_title_label)).setText(article.getTitle());
        ((TextView)findViewById(R.id.article_date_label)).setText(article.getDate());

        LinearLayout articleContent = (LinearLayout)findViewById(R.id.article_content);
        articleContent.removeAllViews();
        Document document = Jsoup.parse(article.getContent());
        for (Element child : document.body().children()) {
            if (child.nodeName() == "h2" || child.nodeName() == "p") {
                TextView textView = new TextView(this);
                textView.setText(child.text());
                if (child.nodeName() == "h2") {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    textView.setTextSize(18);
                } else {
                    textView.setLineSpacing(0, 1.2f);
                }
                if (child.nextElementSibling() != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    textView.setLayoutParams(params);
                }
                articleContent.addView(textView);
            }
        }

        ((ScrollView)findViewById(R.id.article_scroll)).scrollTo(0, 0);

        articlePage.setVisibility(View.VISIBLE);
        articlePage.setAlpha(0f);
        articlePage.setTranslationY(64);
        articlePage.animate().alpha(1).translationY(0).setDuration(150);
    }

    private void hideArticlePage() {
        articlePage.animate().alpha(0).translationY(64).setDuration(150).withEndAction(new Runnable() {
            public void run() {
                articlePage.setVisibility(View.GONE);
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (preferences.getBoolean("dark-theme", false)) {
            setTheme(R.style.dark_app_theme);
        }
        setContentView(R.layout.activity_main);

        // Pages
        mainPage = (LinearLayout)findViewById(R.id.main_page);
        articlePage = (LinearLayout)findViewById(R.id.article_page);

        // Main Page

        // News tabs
        initNewsTab((ListView)findViewById(R.id.latest_articles_list), "http://feeds.nos.nl/nosnieuwsalgemeen", (ImageView)findViewById(R.id.latest_refresh_button));
        initNewsTab((ListView)findViewById(R.id.sports_articles_list), "http://feeds.nos.nl/nossportalgemeen", (ImageView)findViewById(R.id.sports_refresh_button));
        initNewsTab((ListView)findViewById(R.id.economy_articles_list), "http://feeds.nos.nl/nosnieuwseconomie", (ImageView)findViewById(R.id.economy_refresh_button));
        initNewsTab((ListView)findViewById(R.id.tech_articles_list), "http://feeds.nos.nl/nosnieuwstech", (ImageView)findViewById(R.id.tech_refresh_button));

        // Settings tab
        Switch darkThemeSwitch = (Switch)findViewById(R.id.dark_theme_switch);
        darkThemeSwitch.setChecked(preferences.getBoolean("dark-theme", false));

        ((Button)findViewById(R.id.settings_save_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("dark-theme", darkThemeSwitch.isChecked());
                editor.apply();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        // Article page
        ((ImageView)findViewById(R.id.article_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideArticlePage();
            }
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
            buttons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    openTab((int)view.getTag());
                }
            });
        }
    }

    public void onBackPressed() {
        if (articlePage.getVisibility() == View.VISIBLE) {
            hideArticlePage();
        }
        else if (tabs[0].getVisibility() != View.VISIBLE) {
            openTab(0);
        }
        else {
            moveTaskToBack(false);
        }
    }

    private void openTab(int index) {
        for (LinearLayout tab : tabs) {
            if ((int)tab.getTag() == index) {
                if (tab.getVisibility() != View.VISIBLE) {
                    tab.setVisibility(View.VISIBLE);

                    View tabContent = tab.getChildAt(1);
                    tabContent.setAlpha(0f);
                    tabContent.setScaleX(0.98f);
                    tabContent.setScaleY(0.98f);
                    tabContent.animate().alpha(1).scaleX(1).scaleY(1).setDuration(150);
                }
            } else {
                tab.setVisibility(View.GONE);
            }
        }

        for (LinearLayout button : buttons) {
            if ((int)button.getTag() == index) {
                button.animate().alpha(1f).setDuration(150);
            } else {
                button.animate().alpha(0.5f).setDuration(150);
            }
        }
    }

    private void initNewsTab(ListView listView, String rssUrl, ImageView refreshButton) {
        ArticlesAdapter articlesAdapter = new ArticlesAdapter(this);
        listView.setAdapter(articlesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticlePage(articlesAdapter.getItem(position));
            }
        });

        FetchDataTask fetchDataTask = null;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (fetchDataTask != null && fetchDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                    fetchDataTask.cancel(false);
                }
                articlesAdapter.clear();
                fetchNewsData(fetchDataTask, articlesAdapter, rssUrl, false);
            }
        });

        fetchNewsData(fetchDataTask, articlesAdapter, rssUrl, true);
    }

    private void fetchNewsData(FetchDataTask fetchDataTask, ArticlesAdapter articlesAdapter, String rssUrl, boolean loadFromCache) {
        try {
            fetchDataTask = new FetchDataTask(this, "https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(rssUrl, "UTF-8") + "&api_key=" + API_KEY + "&count=20", loadFromCache, true, new FetchDataTask.OnLoadListener() {
                public void onLoad(String data) {
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
                    } catch (Exception e) {}
                }
            });
            fetchDataTask.execute();
        } catch (Exception e) {}
    }
}
