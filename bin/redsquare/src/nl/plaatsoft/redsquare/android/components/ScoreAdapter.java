/*
 * Copyright (c) 2020-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.redsquare.android.components;

import java.util.Objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import nl.plaatsoft.redsquare.android.R;
import nl.plaatsoft.redsquare.android.models.Score;

import org.jspecify.annotations.Nullable;

public class ScoreAdapter extends ArrayAdapter<Score> {
    private static class ViewHolder {
        public @SuppressWarnings("null") TextView scoreName;
        public @SuppressWarnings("null") TextView scoreScore;
    }

    public ScoreAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @SuppressWarnings("null") ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_score, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.scoreName = Objects.requireNonNull(convertView).findViewById(R.id.score_name);
            viewHolder.scoreScore = convertView.findViewById(R.id.score_score);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        var score = getItem(position);
        viewHolder.scoreName.setText(score.name());
        viewHolder.scoreScore.setText(String.valueOf(score.score()));
        return convertView;
    }
}
