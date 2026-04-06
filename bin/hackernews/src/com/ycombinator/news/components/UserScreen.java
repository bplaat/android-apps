/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;
import static com.ycombinator.news.components.Styles.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.react.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import com.ycombinator.news.R;
import com.ycombinator.news.models.User;

public class UserScreen extends Component {
    private final String username;
    private @Nullable User user = null;
    private boolean hasError = false;
    private @Nullable FetchDataTask userTask = null;

    public UserScreen(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    protected void onMount() {
        loadUser();
    }

    private void loadUser() {
        if (userTask != null) userTask.cancel();
        user = null;
        hasError = false;
        rebuild();
        userTask = FetchDataTask.with(getContext())
                       .load("https://hacker-news.firebaseio.com/v0/user/" + username + ".json")
                       .then(
                           data -> {
                               try {
                                   var json = new JSONObject(new String(data, StandardCharsets.UTF_8));
                                   user = User.fromJSON(json);
                                   hasError = false;
                               } catch (JSONException e) {
                                   Log.e(getContext().getPackageName(), "Can't parse user", e);
                                   hasError = true;
                               }
                               rebuild();
                           },
                           e -> {
                               Log.e(getContext().getPackageName(), "Can't fetch user", e);
                               hasError = true;
                               rebuild();
                           })
                       .fetch();
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
                new Text(username).modifier(actionBarTitle());
            }).modifier(actionBar());

            if (hasError) {
                renderDisconnected(this::loadUser);
                return;
            }

            new Column(() -> {
                if (user == null) {
                    new Spacer().modifier(Modifier.of()
                            .width(dp(180))
                            .height(dp(16))
                            .background(R.color.loading_background_color)
                            .margin(dp(0), dp(0), dp(8), dp(0)));
                    new Spacer().modifier(Modifier.of()
                            .width(dp(120))
                            .height(dp(14))
                            .background(R.color.loading_background_color)
                            .margin(dp(0), dp(0), dp(8), dp(0)));
                } else {
                    new Text(context.getString(R.string.user_karma, user.karma()))
                        .modifier(Modifier.of()
                                .width(matchParent())
                                .fontSize(sp(16))
                                .margin(dp(0), dp(0), dp(6), dp(0)));

                    var dateStr = new SimpleDateFormat("MMMM d, yyyy", Locale.US)
                                      .format(new Date(user.created() * 1000));
                    new Text(context.getString(R.string.user_joined, dateStr))
                        .modifier(Modifier.of()
                                .width(matchParent())
                                .fontSize(sp(14))
                                .textColor(R.color.secondary_text_color)
                                .margin(dp(0), dp(0), dp(12), dp(0)));

                    if (user.about() != null && !user.about().isEmpty()) {
                        var c = BuildContext.current();
                        var tv = c.slot(TextView.class, () -> new TextView(c.getContext()));
                        var spanned = Html.fromHtml(user.about(), Html.FROM_HTML_MODE_LEGACY);
                        if (!spanned.toString().equals(tv.getText().toString())) {
                            tv.setText(spanned);
                        }
                        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, sp(14).resolve(context));
                        tv.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
                        var lp = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.bottomMargin = (int)dp(16).resolve(context);
                        tv.setLayoutParams(lp);
                    }

                    var profileUrl = "https://news.ycombinator.com/user?id=" + user.id();
                    new Text(R.string.user_view_submissions)
                        .modifier(Modifier.of()
                                .fontSize(sp(14))
                                .textColor(R.color.tab_selected_color)
                                .backgroundAttr(android.R.attr.selectableItemBackgroundBorderless))
                        .onClick(() -> context.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))));
                }
            }).modifier(Modifier.of().width(matchParent()).height(matchParent()).padding(dp(16)).scrollVertical());
        }).modifier(Modifier.of().width(matchParent()).height(matchParent()));
    }
}
