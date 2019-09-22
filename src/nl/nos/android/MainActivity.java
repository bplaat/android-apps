package nl.nos.android;

import android.app.Activity;
import android.graphics.Typeface;
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
    private static final String NEWS_RSS_FEED = "http://feeds.nos.nl/nosnieuwsalgemeen";
    private static final String SPORTS_RSS_FEED = "http://feeds.nos.nl/nossportalgemeen";
    private static final String API_KEY = "s3khkckng9or74lykjvhufbetd8jgtxcf265ltrh";

    private FetchDataTask fetchNewsDataTask;
    private FetchDataTask fetchSportsDataTask;
    private ArticlesAdapter newsArticlesAdapter;
    private ArticlesAdapter sportsArticlesAdapter;

    private LinearLayout mainPage;
    private LinearLayout articlePage;

    private boolean articlePageOpen = false;

    private void openArticle(Article article) {
        articlePageOpen = true;
        articlePage.setVisibility(View.VISIBLE);
        articlePage.setAlpha(0f);
        articlePage.setTranslationY(64);
        articlePage.animate().alpha(1).translationY(0).setDuration(150);

        ((ScrollView)findViewById(R.id.article_scroll)).scrollTo(0, 0);

        FetchImageTask.fetchImage(this, (ImageView)findViewById(R.id.article_image), article.getImageUrl());
        ((TextView)findViewById(R.id.article_title_label)).setText(article.getTitle());
        ((TextView)findViewById(R.id.article_date_label)).setText(article.getDate());

        LinearLayout articleContent = (LinearLayout)findViewById(R.id.article_content);
        articleContent.removeAllViews();
        Document doc = Jsoup.parse(article.getContent());
        for (Element child : doc.body().children()) {
            if (child.nodeName() == "h2" || child.nodeName() == "p") {
                TextView textView = new TextView(this);
                textView.setText(child.text());
                if (child.nodeName() == "h2") {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    textView.setTextSize(18);
                } else {
                    textView.setLineSpacing(0, 1.3f);
                }
                if (child.nextElementSibling() != null) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    textView.setLayoutParams(params);
                }
                articleContent.addView(textView);
            }
        }
    }

    public void hideArticle() {
        articlePageOpen = false;
        articlePage.animate().alpha(0).translationY(64).setDuration(150).withEndAction(new Runnable() {
            public void run() {
                articlePage.setVisibility(View.GONE);
            }
        });
    }

    public void onBackPressed() {
        if (articlePageOpen) {
            hideArticle();
        } else {
            super.onBackPressed();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pages
        mainPage = (LinearLayout)findViewById(R.id.main_page);
        articlePage = (LinearLayout)findViewById(R.id.article_page);

        // Main Page

        // News tab
        ListView newsArticlesList = (ListView)findViewById(R.id.news_articles_list);
        newsArticlesList.setAdapter(newsArticlesAdapter = new ArticlesAdapter(this));
        newsArticlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticle(newsArticlesAdapter.getItem(position));
            }
        });

        ((ImageView)findViewById(R.id.news_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fetchNewsDataTask.cancel(false);
                newsArticlesAdapter.clear();
                fetchNewsData(false);
            }
        });

        // Sports tab
        ListView sportsArticlesList = (ListView)findViewById(R.id.sports_articles_list);
        sportsArticlesList.setAdapter(sportsArticlesAdapter = new ArticlesAdapter(this));
        sportsArticlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openArticle(sportsArticlesAdapter.getItem(position));
            }
        });

        ((ImageView)findViewById(R.id.sports_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fetchSportsDataTask.cancel(false);
                sportsArticlesAdapter.clear();
                fetchSportsData(false);
            }
        });

        // Article page
        ((ImageView)findViewById(R.id.article_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideArticle();
            }
        });

        // Bottom bar
        LinearLayout newsTab = (LinearLayout)findViewById(R.id.news_tab);
        LinearLayout sportsTab = (LinearLayout)findViewById(R.id.sports_tab);
        LinearLayout settingsTab = (LinearLayout)findViewById(R.id.settings_tab);

        LinearLayout newsButton = (LinearLayout)findViewById(R.id.news_button);
        LinearLayout sportsButton = (LinearLayout)findViewById(R.id.sports_button);
        LinearLayout settingsButton = (LinearLayout)findViewById(R.id.settings_button);

        newsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                newsTab.setVisibility(View.VISIBLE);
                sportsTab.setVisibility(View.GONE);
                settingsTab.setVisibility(View.GONE);

                newsArticlesList.setAlpha(0f);
                newsArticlesList.setScaleX(0.98f);
                newsArticlesList.setScaleY(0.98f);
                newsArticlesList.animate().alpha(1).scaleX(1).scaleY(1).setDuration(200);

                newsButton.animate().alpha(1f).setDuration(150);
                sportsButton.animate().alpha(0.5f).setDuration(150);
                settingsButton.animate().alpha(0.5f).setDuration(150);
            }
        });

        sportsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                newsTab.setVisibility(View.GONE);
                sportsTab.setVisibility(View.VISIBLE);
                settingsTab.setVisibility(View.GONE);

                sportsArticlesList.setAlpha(0f);
                sportsArticlesList.setScaleX(0.98f);
                sportsArticlesList.setScaleY(0.98f);
                sportsArticlesList.animate().alpha(1).scaleX(1).scaleY(1).setDuration(200);

                newsButton.animate().alpha(0.5f).setDuration(150);
                sportsButton.animate().alpha(1f).setDuration(150);
                settingsButton.animate().alpha(0.5f).setDuration(150);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                newsTab.setVisibility(View.GONE);
                sportsTab.setVisibility(View.GONE);
                settingsTab.setVisibility(View.VISIBLE);

                settingsTab.setAlpha(0f);
                settingsTab.setScaleX(0.98f);
                settingsTab.setScaleY(0.98f);
                settingsTab.animate().alpha(1).scaleX(1).scaleY(1).setDuration(200);

                newsButton.animate().alpha(0.5f).setDuration(150);
                sportsButton.animate().alpha(0.5f).setDuration(150);
                settingsButton.animate().alpha(1f).setDuration(150);
            }
        });

        fetchNewsData(true);
        fetchSportsData(true);
    }

    private void fetchNewsData (boolean loadFromCache) {
        try {
            fetchNewsDataTask = new FetchDataTask(this, "https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(NEWS_RSS_FEED, "UTF-8") + "&api_key=" + API_KEY + "&count=20", loadFromCache, true, new FetchDataTask.OnLoadListener() {
                public void onLoad(String data) {
                    try {
                        JSONObject feed = new JSONObject(data);
                        JSONArray articles = feed.getJSONArray("items");
                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);
                            newsArticlesAdapter.add(new Article(
                                article.getString("title"),
                                article.getJSONObject("enclosure").getString("link"),
                                article.getString("pubDate"),
                                article.getString("content")
                            ));
                        }
                    } catch (Exception e) {}
                }
            });
            fetchNewsDataTask.execute();
        } catch (Exception e) {}
    }

    private void fetchSportsData (boolean loadFromCache) {
        try {
            fetchSportsDataTask = new FetchDataTask(this, "https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(SPORTS_RSS_FEED, "UTF-8") + "&api_key=" + API_KEY + "&count=20", loadFromCache, true, new FetchDataTask.OnLoadListener() {
                public void onLoad(String data) {
                    try {
                        JSONObject feed = new JSONObject(data);
                        JSONArray articles = feed.getJSONArray("items");
                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);
                            sportsArticlesAdapter.add(new Article(
                                article.getString("title"),
                                article.getJSONObject("enclosure").getString("link"),
                                article.getString("pubDate"),
                                article.getString("content")
                            ));
                        }
                    } catch (Exception e) {}
                }
            });
            fetchSportsDataTask.execute();
        } catch (Exception e) {}
    }
}
