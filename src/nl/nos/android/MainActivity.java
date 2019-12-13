package nl.nos.android;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

public class MainActivity extends Activity {
    private static final String API_KEY = "s3khkckng9or74lykjvhufbetd8jgtxcf265ltrh";
    private static final String LATEST_RSS_URL = "http://feeds.nos.nl/nosnieuwsalgemeen";
    private static final String SPORTS_RSS_URL = "http://feeds.nos.nl/nossportalgemeen";
    private static final String TECH_RSS_URL = "http://feeds.nos.nl/nosnieuwstech";

    private LinearLayout mainPage;
    private LinearLayout articlePage;

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

    public void hideArticlePage() {
        articlePage.animate().alpha(0).translationY(64).setDuration(150).withEndAction(new Runnable() {
            public void run() {
                articlePage.setVisibility(View.GONE);
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pages
        mainPage = (LinearLayout)findViewById(R.id.main_page);
        articlePage = (LinearLayout)findViewById(R.id.article_page);

        // Main Page

        // Latest tab
        ListView latestArticlesList = (ListView)findViewById(R.id.latest_articles_list);
        ArticlesAdapter latestArticlesAdapter = new ArticlesAdapter(this);
        latestArticlesList.setAdapter(latestArticlesAdapter);
        latestArticlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticlePage(latestArticlesAdapter.getItem(position));
            }
        });
        FetchDataTask latestFetchDataTask = null;
        ((ImageView)findViewById(R.id.latest_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (latestFetchDataTask != null && latestFetchDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                    latestFetchDataTask.cancel(false);
                }
                latestArticlesAdapter.clear();
                fetchData(latestFetchDataTask, latestArticlesAdapter, LATEST_RSS_URL, false);
            }
        });
        fetchData(latestFetchDataTask, latestArticlesAdapter, LATEST_RSS_URL, true);

        // Sports tab
        ListView sportsArticlesList = (ListView)findViewById(R.id.sports_articles_list);
        ArticlesAdapter sportsArticlesAdapter = new ArticlesAdapter(this);
        sportsArticlesList.setAdapter(sportsArticlesAdapter);
        sportsArticlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticlePage(sportsArticlesAdapter.getItem(position));
            }
        });
        FetchDataTask sportsFetchDataTask = null;
        ((ImageView)findViewById(R.id.sports_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (sportsFetchDataTask != null && sportsFetchDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                    sportsFetchDataTask.cancel(false);
                }
                sportsArticlesAdapter.clear();
                fetchData(sportsFetchDataTask, sportsArticlesAdapter, SPORTS_RSS_URL, false);
            }
        });
        fetchData(sportsFetchDataTask, sportsArticlesAdapter, SPORTS_RSS_URL, true);

        // Tech tab
        ListView techArticlesList = (ListView)findViewById(R.id.tech_articles_list);
        ArticlesAdapter techArticlesAdapter = new ArticlesAdapter(this);
        techArticlesList.setAdapter(techArticlesAdapter);
        techArticlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticlePage(techArticlesAdapter.getItem(position));
            }
        });
        FetchDataTask techFetchDataTask = null;
        ((ImageView)findViewById(R.id.tech_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (techFetchDataTask != null && techFetchDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                    techFetchDataTask.cancel(false);
                }
                techArticlesAdapter.clear();
                fetchData(techFetchDataTask, techArticlesAdapter, TECH_RSS_URL, false);
            }
        });
        fetchData(techFetchDataTask, techArticlesAdapter, TECH_RSS_URL, true);

        // Article page
        ((ImageView)findViewById(R.id.article_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideArticlePage();
            }
        });

        // Bottom bar
        LinearLayout tabs[] = {
            (LinearLayout)findViewById(R.id.latest_tab),
            (LinearLayout)findViewById(R.id.sports_tab),
            (LinearLayout)findViewById(R.id.tech_tab),
            (LinearLayout)findViewById(R.id.settings_tab)
        };

        LinearLayout buttons[] = {
            (LinearLayout)findViewById(R.id.latest_button),
            (LinearLayout)findViewById(R.id.sports_button),
            (LinearLayout)findViewById(R.id.tech_button),
            (LinearLayout)findViewById(R.id.settings_button)
        };

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setTag(i);
            buttons[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    int index = (int)view.getTag();

                    if (tabs[index].getVisibility() != View.VISIBLE) {
                        View tabContent = tabs[index].getChildAt(1);
                        tabContent.setAlpha(0f);
                        tabContent.setScaleX(0.98f);
                        tabContent.setScaleY(0.98f);
                        tabContent.animate().alpha(1).scaleX(1).scaleY(1).setDuration(150);
                    }

                    for (LinearLayout tab : tabs) {
                        tab.setVisibility(View.GONE);
                    }
                    tabs[index].setVisibility(View.VISIBLE);

                    for (LinearLayout button : buttons) {
                        button.animate().alpha(0.5f).setDuration(150);
                    }
                    view.animate().alpha(1f).setDuration(150);
                }
            });
        }
    }

    public void onBackPressed() {
        if (articlePage.getVisibility() == View.VISIBLE) {
            hideArticlePage();
        } else {
            super.onBackPressed();
        }
    }

    private void fetchData (FetchDataTask fetchDataTask, ArticlesAdapter articlesAdapter, String rssUrl, boolean loadFromCache) {
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
