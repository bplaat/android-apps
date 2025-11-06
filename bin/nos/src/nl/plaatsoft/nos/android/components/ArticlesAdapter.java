/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.nos.android.R;
import nl.plaatsoft.nos.android.models.Article;

public class ArticlesAdapter extends ArrayAdapter<Article> {
    private static class ViewHolder {
        public ImageView articleItemImage;
        public TextView articleItemTitleLabel;
    }

    public ArticlesAdapter(Context context) {
        super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_article, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.articleItemImage = (ImageView)convertView.findViewById(R.id.article_item_image);
            viewHolder.articleItemTitleLabel = (TextView)convertView.findViewById(R.id.article_item_title_label);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        var article = getItem(position);
        FetchImageTask.with(getContext())
            .load(article.imageUrl())
            .fadeIn()
            .loadingColor(ContextCompat.getColor(getContext(), R.color.loading_color))
            .into(viewHolder.articleItemImage)
            .fetch();
        viewHolder.articleItemTitleLabel.setText(article.title());
        return convertView;
    }
}
