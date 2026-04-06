/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;

import java.util.function.Consumer;

import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

import nl.plaatsoft.android.react.*;

import com.ycombinator.news.R;
import com.ycombinator.news.models.Comment;

public class CommentItem {
    private static final int INDENT_PER_LEVEL = 12;

    @SuppressWarnings("deprecation")
    public CommentItem(Comment comment, Consumer<Comment> onToggleCollapse) {
        var context = BuildContext.current().getContext();
        var indent = dp(INDENT_PER_LEVEL * comment.depth()).resolve(context);

        if (comment.isPlaceholder()) {
            new Column(() -> {
                // Header row: username (left) + time (right)
                new Row(() -> {
                    new Spacer().modifier(Modifier.of()
                            .weight(1)
                            .height(dp(14))
                            .background(R.color.loading_background_color));
                    new Spacer().modifier(Modifier.of()
                            .width(dp(50))
                            .height(dp(13))
                            .background(R.color.loading_background_color)
                            .margin(dp(0), dp(8), dp(0), dp(0)));
                }).modifier(Modifier.of().width(matchParent()).margin(dp(0), dp(0), dp(8), dp(0)));
                // Body text lines
                new Spacer().modifier(Modifier.of()
                        .width(matchParent())
                        .height(dp(14))
                        .background(R.color.loading_background_color)
                        .margin(dp(0), dp(0), dp(4), dp(0)));
                new Spacer().modifier(Modifier.of()
                        .width(dp(220))
                        .height(dp(14))
                        .background(R.color.loading_background_color));
            })
                .modifier(Modifier.of()
                        .width(matchParent())
                        .padding(dp(12), dp(16), dp(12), px(indent + dp(16).resolve(context)))
                        .backgroundAttr(android.R.attr.selectableItemBackground));
            return;
        }

        new Column(() -> {
            new Row(() -> {
                new Text(comment.by().isEmpty() ? context.getString(R.string.comment_deleted) : comment.by())
                    .modifier(Modifier.of()
                            .weight(1)
                            .fontSize(sp(13))
                            .fontWeight(Modifier.FontWeight.MEDIUM)
                            .textSingleLine());
                new Text(StoryItem.timeAgo(context, comment.time()))
                    .modifier(Modifier.of()
                            .fontSize(sp(12))
                            .textColor(R.color.secondary_text_color)
                            .margin(dp(0), dp(8), dp(0), dp(0)));
            }).modifier(Modifier.of().width(matchParent()).margin(dp(0), dp(0), dp(4), dp(0)));

            if (!comment.collapsed() && !comment.text().isEmpty()) {
                var c = BuildContext.current();
                var tv = c.slot(TextView.class, () -> new TextView(c.getContext()));
                var spanned = Html.fromHtml(comment.text(), Html.FROM_HTML_MODE_LEGACY);
                if (!spanned.toString().equals(tv.getText().toString())) {
                    tv.setText(spanned);
                }
                tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, sp(14).resolve(context));
                tv.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
                var lp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = (int)dp(4).resolve(context);
                tv.setLayoutParams(lp);
            }

            if (comment.collapsed() && comment.kids().length > 0) {
                new Text(context.getString(R.string.comment_replies, comment.kids().length))
                    .modifier(Modifier.of()
                            .fontSize(sp(12))
                            .textColor(R.color.secondary_text_color)
                            .margin(dp(4), dp(0), dp(0), dp(0)));
            }
        })
            .modifier(Modifier.of()
                    .width(matchParent())
                    .padding(dp(12), dp(16), dp(12), px(indent + dp(16).resolve(context)))
                    .backgroundAttr(android.R.attr.selectableItemBackground))
            .onClick(() -> onToggleCollapse.accept(comment));
    }
}
