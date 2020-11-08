package nl.plaatsoft.nos.android;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

public class ArticleActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Article article = (Article)getIntent().getSerializableExtra("article");

        ((ImageView)findViewById(R.id.article_back_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        new FetchImageTask(this, (ImageView)findViewById(R.id.article_image), article.getImageUrl(), true, true);
        ((TextView)findViewById(R.id.article_title_label)).setText(article.getTitle());
        ((TextView)findViewById(R.id.article_date_label)).setText(article.getDate());

        LinearLayout articleContent = (LinearLayout)findViewById(R.id.article_content);
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
    }
}
