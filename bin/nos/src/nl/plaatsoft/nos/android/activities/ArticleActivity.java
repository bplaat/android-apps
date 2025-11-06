/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.nos.android.R;
import nl.plaatsoft.nos.android.models.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ArticleActivity extends BaseActivity {
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        useWindowInsets(findViewById(R.id.article_scroll));

        var article = (Article)getIntent().getSerializableExtra("article");

        ((ImageView)findViewById(R.id.article_back_button)).setOnClickListener((View view) -> { finish(); });

        FetchImageTask.with(this)
            .load(article.imageUrl())
            .fadeIn()
            .loadingColor(ContextCompat.getColor(this, R.color.loading_color))
            .into(findViewById(R.id.article_image))
            .fetch();
        ((TextView)findViewById(R.id.article_title_label)).setText(article.title());
        ((TextView)findViewById(R.id.article_date_label)).setText(article.date());

        var articleContent = (LinearLayout)findViewById(R.id.article_content);
        var document = Jsoup.parse(article.content());
        for (var child : document.body().children()) {
            if (child.nodeName() == "h2" || child.nodeName() == "p") {
                var textView = new TextView(this);
                textView.setText(child.text());

                if (child.nodeName() == "h2") {
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    textView.setTextSize(18);
                } else {
                    textView.setLineSpacing(0, 1.2f);
                }

                if (child.nextElementSibling() != null) {
                    var params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 0,
                        (int)TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    textView.setLayoutParams(params);
                }

                articleContent.addView(textView);
            }
        }
    }
}
