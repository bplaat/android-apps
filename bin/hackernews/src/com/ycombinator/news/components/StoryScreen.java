/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;
import static com.ycombinator.news.components.Styles.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.react.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import com.ycombinator.news.R;
import com.ycombinator.news.models.Comment;
import com.ycombinator.news.models.Story;

public class StoryScreen extends Component {
    private final int storyId;
    private final Consumer<String> openUser;
    private @Nullable Story story = null;
    private final List<Comment> comments = new ArrayList<>();
    private final Map<Integer, Integer> commentIndex = new HashMap<>();
    private boolean hasError = false;
    private @Nullable FetchDataTask storyTask = null;
    private final List<FetchDataTask> commentTasks = new ArrayList<>();

    public StoryScreen(Context context, int storyId, Consumer<String> openUser) {
        super(context);
        this.storyId = storyId;
        this.openUser = openUser;
    }

    @Override
    protected void onMount() {
        loadStory();
    }

    private void loadStory() {
        if (storyTask != null) storyTask.cancel();
        for (var task : commentTasks) task.cancel();
        commentTasks.clear();
        story = null;
        comments.clear();
        commentIndex.clear();
        hasError = false;
        rebuild();
        storyTask = FetchDataTask.with(getContext())
                        .load("https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json")
                        .then(
                            data -> {
                                try {
                                    var json = new JSONObject(new String(data, StandardCharsets.UTF_8));
                                    story = Story.fromJSON(json, 1);
                                    hasError = false;
                                    if (story.kids().length > 0) {
                                        loadComments(story.kids(), -1, 0);
                                    }
                                    rebuild();
                                } catch (JSONException e) {
                                    Log.e(getContext().getPackageName(), "Can't parse story", e);
                                    hasError = true;
                                    rebuild();
                                }
                            },
                            e -> {
                                Log.e(getContext().getPackageName(), "Can't fetch story", e);
                                hasError = true;
                                rebuild();
                            })
                        .fetch();
    }

    // Inserts placeholders for `ids` into the comments list immediately after `afterIndex`,
    // then fetches each comment. All mutations happen on the main thread, so no synchronization needed.
    private void loadComments(int[] ids, int afterIndex, int depth) {
        var insertAt = afterIndex + 1;
        for (var i = 0; i < ids.length; i++) {
            comments.add(insertAt + i, Comment.placeholder(ids[i], depth));
            // Shift all later indices
            for (var entry : commentIndex.entrySet()) {
                if (entry.getValue() >= insertAt + i) {
                    entry.setValue(entry.getValue() + 1);
                }
            }
            commentIndex.put(ids[i], insertAt + i);
        }
        rebuild();

        for (var i = 0; i < ids.length; i++) {
            final int commentId = ids[i];
            final int finalDepth = depth;
            final var taskRef = new FetchDataTask[1];
            taskRef[0] = FetchDataTask.with(getContext())
                             .load("https://hacker-news.firebaseio.com/v0/item/" + commentId + ".json")
                             .then(
                                 data -> {
                                     commentTasks.remove(taskRef[0]);
                                     try {
                                         var json = new JSONObject(new String(data, StandardCharsets.UTF_8));
                                         if (json.optBoolean("deleted") || json.optBoolean("dead")) {
                                             // Remove the placeholder
                                             var idx = commentIndex.get(commentId);
                                             if (idx != null) {
                                                 comments.remove((int)idx);
                                                 commentIndex.remove(commentId);
                                                 for (var entry : commentIndex.entrySet()) {
                                                     if (entry.getValue() > idx) {
                                                         entry.setValue(entry.getValue() - 1);
                                                     }
                                                 }
                                             }
                                         } else {
                                             var comment = Comment.fromJSON(json, finalDepth);
                                             var idx = commentIndex.get(commentId);
                                             if (idx != null) {
                                                 comments.set(idx, comment);
                                                 if (comment.kids().length > 0) {
                                                     loadComments(comment.kids(), idx, finalDepth + 1);
                                                 }
                                             }
                                         }
                                         rebuild();
                                     } catch (JSONException e) {
                                         Log.e(getContext().getPackageName(), "Can't parse comment", e);
                                     }
                                 },
                                 e -> {
                                     commentTasks.remove(taskRef[0]);
                                     Log.e(getContext().getPackageName(), "Can't fetch comment", e);
                                 })
                             .fetch();
            commentTasks.add(taskRef[0]);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render() {
        var context = getContext();

        new Column(() -> {
            new Row(() -> {
                new ImageButton(R.drawable.ic_arrow_left)
                    .onClick(() -> ((Activity)context).finish())
                    .modifier(actionBarIconButton());
                new Text(story != null ? story.title() : "HackerNews")
                    .modifier(actionBarTitle());
                if (story != null && story.url() != null) {
                    var url = story.url();
                    new ImageButton(R.drawable.ic_open_in_browser)
                        .onClick(() -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))))
                        .modifier(actionBarIconButton());
                }
                if (story != null) {
                    var shareUrl = story.url() != null ? story.url()
                        : "https://news.ycombinator.com/item?id=" + story.id();
                    var shareTitle = story.title();
                    new ImageButton(R.drawable.ic_share_variant)
                        .onClick(() -> {
                            var intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT,
                                context.getString(R.string.story_detail_share_message, shareTitle, shareUrl));
                            context.startActivity(Intent.createChooser(intent, null));
                        })
                        .modifier(actionBarIconButton());
                }
            }).modifier(actionBar());

            if (story == null && hasError) {
                renderDisconnected(this::loadStory);
                return;
            }

            // Story header + comments in single scrollable list
            var listItems = new ArrayList<Object>();
            listItems.add("header");
            listItems.addAll(comments);

            new LazyColumn<>(listItems, item -> item instanceof Comment c ? c.id() : 0, item -> {
                if ("header".equals(item)) {
                    renderHeader(context);
                } else if (item instanceof Comment comment) {
                    new CommentItem(comment, this::toggleCollapse);
                }
            }).modifier(Modifier.of().width(matchParent()).weight(1).useWindowInsets());
        }).modifier(Modifier.of().width(matchParent()).height(matchParent()));
    }

    @SuppressWarnings("deprecation")
    private void renderHeader(Context context) {
        new Column(() -> {
            if (story == null) {
                new Spacer().modifier(Modifier.of()
                        .width(dp(220))
                        .height(dp(18))
                        .background(R.color.loading_background_color)
                        .margin(dp(0), dp(0), dp(6), dp(0)));
                new Spacer().modifier(Modifier.of()
                        .width(dp(140))
                        .height(dp(14))
                        .background(R.color.loading_background_color));
                return;
            }

            new Text(story.title())
                .modifier(Modifier.of()
                        .width(matchParent())
                        .fontSize(sp(18))
                        .fontWeight(Modifier.FontWeight.MEDIUM)
                        .margin(dp(0), dp(0), dp(8), dp(0)));

            var domain = story.domain();
            if (domain != null) {
                new Text("(" + domain + ")")
                    .modifier(Modifier.of()
                            .fontSize(sp(13))
                            .textColor(R.color.secondary_text_color)
                            .margin(dp(0), dp(0), dp(4), dp(0)));
            }

            new Text(context.getString(R.string.story_points, story.score())
                + " " + context.getString(R.string.story_by, story.by())
                + " " + StoryItem.timeAgo(context, story.time())
                + " | " + (story.descendants() == 0
                    ? context.getString(R.string.story_no_comments)
                    : context.getString(R.string.story_comments, story.descendants())))
                .modifier(Modifier.of()
                        .fontSize(sp(13))
                        .textColor(R.color.secondary_text_color)
                        .margin(dp(0), dp(0), dp(8), dp(0)));

            // Author link
            var byUser = story.by();
            new Text(context.getString(R.string.story_by, byUser))
                .modifier(Modifier.of()
                        .fontSize(sp(13))
                        .textColor(R.color.tab_selected_color)
                        .backgroundAttr(android.R.attr.selectableItemBackgroundBorderless))
                .onClick(() -> openUser.accept(byUser));

            // Self-post body text (Ask HN, Show HN, jobs, etc.)
            var storyText = story.text();
            if (storyText != null && !storyText.isEmpty()) {
                var c = BuildContext.current();
                var tv = c.slot(TextView.class, () -> new TextView(c.getContext()));
                var spanned = Html.fromHtml(storyText, Html.FROM_HTML_MODE_LEGACY);
                if (!spanned.toString().equals(tv.getText().toString())) {
                    tv.setText(spanned);
                }
                tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, sp(14).resolve(context));
                tv.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
                var lp = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = (int)dp(8).resolve(context);
                tv.setLayoutParams(lp);
            }

            // HN discussion link
            var hnUrl = "https://news.ycombinator.com/item?id=" + story.id();
            new Text(R.string.story_detail_open_hn)
                .modifier(Modifier.of()
                        .fontSize(sp(13))
                        .textColor(R.color.tab_selected_color)
                        .margin(dp(4), dp(0), dp(0), dp(0))
                        .backgroundAttr(android.R.attr.selectableItemBackgroundBorderless))
                .onClick(() -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(hnUrl))));

            if (comments.isEmpty() && !hasError) {
                new Text(R.string.story_detail_no_comments)
                    .modifier(Modifier.of()
                            .width(matchParent())
                            .padding(dp(16))
                            .textGravity(Gravity.CENTER_HORIZONTAL)
                            .textColor(R.color.secondary_text_color));
            }
        }).modifier(Modifier.of().width(matchParent()).padding(dp(16)));
    }

    private void toggleCollapse(Comment comment) {
        var idx = commentIndex.get(comment.id());
        if (idx == null)
            return;
        var updated = comment.withCollapsed(!comment.collapsed());
        comments.set(idx, updated);
        rebuild();
    }
}
