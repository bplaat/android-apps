/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;

import android.view.Gravity;

import nl.plaatsoft.android.react.*;

import com.ycombinator.news.R;
import com.ycombinator.news.models.Story;

public class StoryItem {
    public StoryItem(Story story, Runnable onTap) {
        if (story.isLoadMore()) {
            new Text(R.string.story_load_more)
                .modifier(Modifier.of()
                        .width(matchParent())
                        .padding(dp(16))
                        .textGravity(Gravity.CENTER_HORIZONTAL)
                        .textColor(R.color.secondary_text_color)
                        .backgroundAttr(android.R.attr.selectableItemBackground))
                .onClick(onTap);
            return;
        }

        var context = BuildContext.current().getContext();

        new Column(() -> {
            if (story.isPlaceholder()) {
                new Row(() -> {
                    new Spacer().modifier(Modifier.of()
                            .minWidth(dp(32))
                            .height(dp(16))
                            .background(R.color.loading_background_color)
                            .align(Gravity.TOP));
                    new Column(() -> {
                        new Spacer().modifier(Modifier.of()
                                .width(dp(180))
                                .height(dp(16))
                                .background(R.color.loading_background_color));
                        new Spacer().modifier(Modifier.of()
                                .width(dp(120))
                                .height(dp(13))
                                .background(R.color.loading_background_color)
                                .margin(dp(4), dp(0), dp(0), dp(0)));
                    }).modifier(Modifier.of().weight(1).margin(dp(0), dp(0), dp(0), dp(16)));
                });
            } else {
                new Row(() -> {
                    new Text(String.valueOf(story.rank()))
                        .modifier(Modifier.of()
                                .minWidth(dp(32))
                                .fontSize(sp(16))
                                .textColor(R.color.secondary_text_color)
                                .align(Gravity.TOP));
                    new Column(() -> {
                        new Text(story.title())
                            .modifier(Modifier.of().width(matchParent()).fontSize(sp(15)));
                        var domain = story.domain();
                        if (domain != null) {
                            new Text("(" + domain + ")")
                                .modifier(Modifier.of()
                                        .fontSize(sp(12))
                                        .textColor(R.color.secondary_text_color)
                                        .margin(dp(2), dp(0), dp(0), dp(0)));
                        }
                        new Text(context.getString(R.string.story_points, story.score())
                            + " " + context.getString(R.string.story_by, story.by())
                            + " " + timeAgo(context, story.time())
                            + " | "
                            + (story.descendants() == 0
                                ? context.getString(R.string.story_no_comments)
                                : context.getString(R.string.story_comments, story.descendants())))
                            .modifier(Modifier.of()
                                    .fontSize(sp(12))
                                    .textColor(R.color.secondary_text_color)
                                    .margin(dp(2), dp(0), dp(0), dp(0)));
                    }).modifier(Modifier.of().weight(1));
                });
            }
        })
            .modifier(Modifier.of()
                    .width(matchParent())
                    .padding(dp(12), dp(16))
                    .backgroundAttr(android.R.attr.selectableItemBackground))
            .onClick(onTap);
    }

    static String timeAgo(android.content.Context context, long unixTime) {
        var seconds = (System.currentTimeMillis() / 1000) - unixTime;
        if (seconds < 60)
            return context.getString(R.string.time_just_now);
        var minutes = seconds / 60;
        if (minutes < 60)
            return context.getString(minutes == 1 ? R.string.time_minute_ago : R.string.time_minutes_ago, minutes);
        var hours = minutes / 60;
        if (hours < 24)
            return context.getString(hours == 1 ? R.string.time_hour_ago : R.string.time_hours_ago, hours);
        var days = hours / 24;
        if (days < 30)
            return context.getString(days == 1 ? R.string.time_day_ago : R.string.time_days_ago, days);
        var months = days / 30;
        if (months < 12)
            return context.getString(months == 1 ? R.string.time_month_ago : R.string.time_months_ago, months);
        var years = days / 365;
        return context.getString(years == 1 ? R.string.time_year_ago : R.string.time_years_ago, years);
    }
}
