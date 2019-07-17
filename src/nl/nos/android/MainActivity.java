package nl.nos.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import java.net.URLEncoder;
import org.json.JSONObject;
import org.json.JSONArray;

public class MainActivity extends Activity {
    public static final String RSS_FEED = "http://feeds.nos.nl/nosnieuwsalgemeen";
    public static final String API_KEY = "s3khkckng9or74lykjvhufbetd8jgtxcf265ltrh";

    public FetchDataTask fetchDataTask;
    public ItemsAdapter itemsAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listItemsList = (ListView)findViewById(R.id.list_items_list);
        listItemsList.setAdapter(itemsAdapter = new ItemsAdapter(this));
        listItemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                intent.putExtra("item", itemsAdapter.getItem(position));
                startActivity(intent);
            }
        });

        ((ImageView)findViewById(R.id.list_refresh_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchDataTask.cancel(false);
                itemsAdapter.clear();
                ImageCache.getInstance().evictAll();
                fetchData();
            }
        });

        fetchData();
    }

    public void onDestroy() {
        ImageCache.getInstance().evictAll();
        super.onDestroy();
    }

    public void fetchData () {
        try {
            fetchDataTask = new FetchDataTask("https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(RSS_FEED, "UTF-8") + "&api_key=" + API_KEY + "&count=20", new FetchDataTask.OnLoadListener() {
                public void onLoad(String data) {
                    try {
                        JSONObject feed = new JSONObject(data);
                        JSONArray items = feed.getJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            itemsAdapter.add(new Item(
                                item.getString("title"),
                                item.getJSONObject("enclosure").getString("link"),
                                item.getString("pubDate"),
                                item.getString("content")
                            ));
                        }
                    } catch (Exception e) {}
                }
            });
            fetchDataTask.execute();
        } catch (Exception e) {}
    }
}
