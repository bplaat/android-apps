/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.compat.IntentCompat;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.nos.android.HtmlParser;
import nl.plaatsoft.nos.android.R;
import nl.plaatsoft.nos.android.models.Article;
import nl.plaatsoft.nos.android.views.AspectRatioImageView;

public class ArticleActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        var scroll = (ScrollView)findViewById(R.id.article_scroll);
        if (savedInstanceState == null)
            scroll.post(() -> scroll.scrollTo(0, 0));
        useWindowInsets(scroll);

        var article = IntentCompat.getSerializableExtra(getIntent(), "article", Article.class);

        findViewById(R.id.article_back_button).setOnClickListener(view -> { finish(); });

        findViewById(R.id.activity_share_button).setOnClickListener(view -> {
            var shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, article.title() + " - " + article.externUrl());
            startActivity(Intent.createChooser(shareIntent, null));
        });

        var articleImage = (AspectRatioImageView)findViewById(R.id.article_image);
        articleImage.setOnClickListener(view -> {
            var intent = new Intent(this, ImageActivity.class);
            intent.putExtra("url", article.imageUrl());
            startActivity(intent);
        });
        FetchImageTask.with(this)
            .load(article.imageUrl())
            .fadeIn()
            .loadingColor(ContextCompat.getColor(this, R.color.loading_color))
            .into(articleImage)
            .fetch();

        // Fill article contents
        var contents = (TextView)findViewById(R.id.article_contents);

        var spannable = new SpannableStringBuilder();

        var titleSpannable = new SpannableStringBuilder(article.title() + "\n");
        titleSpannable.setSpan(
            new StyleSpan(Typeface.BOLD), 0, titleSpannable.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleSpannable.setSpan(
            new RelativeSizeSpan(1.25f), 0, titleSpannable.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(titleSpannable);

        var newlineSpannable = new SpannableStringBuilder("\n");
        newlineSpannable.setSpan(new RelativeSizeSpan(0.5f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(newlineSpannable);

        var dateSpannable =
            new SpannableStringBuilder(getString(R.string.article_date_label, article.date().split(" \\+")[0]) + "\n");
        dateSpannable.setSpan(
            new StyleSpan(Typeface.ITALIC), 0, dateSpannable.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        dateSpannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.secondary_text_color)), 0,
            dateSpannable.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(dateSpannable);

        var newlineSpannable2 = new SpannableStringBuilder("\n");
        newlineSpannable2.setSpan(new RelativeSizeSpan(0.5f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append(newlineSpannable2);

        var elements = HtmlParser.parse(article.content());
        for (var i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            var isLast = i == elements.size() - 1;
            if (element.tag().equals("h2") || element.tag().equals("p")) {
                var spannablePart = new SpannableStringBuilder(element.text() + (!isLast ? "\n" : ""));
                if (element.tag().equals("h2")) {
                    var length = spannablePart.length() - (!isLast ? 1 : 0);
                    spannablePart.setSpan(new StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannablePart.setSpan(new RelativeSizeSpan(1.11f), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                spannable.append(spannablePart);

                if (!isLast) {
                    var newlineSpannable3 = new SpannableStringBuilder("\n");
                    newlineSpannable3.setSpan(new RelativeSizeSpan(0.5f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.append(newlineSpannable3);
                }
            }
        }

        contents.setText(spannable);
    }
}
