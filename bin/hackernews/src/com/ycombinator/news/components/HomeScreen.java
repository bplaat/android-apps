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
import java.util.List;
import java.util.function.IntConsumer;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.react.*;

import com.ycombinator.news.R;
import com.ycombinator.news.Settings;
import com.ycombinator.news.models.Story;

public class HomeScreen extends Component {
    private static final int PAGE_SIZE = 30;

    private static final int TAB_TOP = 0;
    private static final int TAB_NEW = 1;
    private static final int TAB_BEST = 2;
    private static final int TAB_ASK = 3;
    private static final int TAB_SHOW = 4;
    private static final int TAB_JOBS = 5;

    private static final String[] TAB_ENDPOINTS = {
        "topstories", "newstories", "beststories", "askstories", "showstories", "jobstories"
    };

    private static final int[] TAB_ICONS = {
        R.drawable.ic_trending_up,
        R.drawable.ic_clock_outline,
        R.drawable.ic_star,
        R.drawable.ic_comment,
        R.drawable.ic_open_in_browser,
        R.drawable.ic_account
    };

    private final Runnable openSettings;
    private final IntConsumer openStory;
    private final Settings settings;
    private int selectedTab = TAB_TOP;
    private final TabState[] tabStates = new TabState[TAB_ENDPOINTS.length];
    private int oldTheme = -1;
    private int oldLanguage = -1;

    private static class TabState {
        int[] storyIds = new int[0];
        int loadedCount = 0;
        List<Story> stories = new ArrayList<>();
        boolean isLoading = false;
        boolean hasError = false;
        @Nullable FetchDataTask idsTask = null;
        final List<FetchDataTask> pendingTasks = new ArrayList<>();
    }

    private TabState state() {
        if (tabStates[selectedTab] == null) tabStates[selectedTab] = new TabState();
        return tabStates[selectedTab];
    }

    public HomeScreen(Context context, Runnable openSettings, IntConsumer openStory) {
        super(context);
        this.openSettings = openSettings;
        this.openStory = openStory;
        settings = new Settings(context);
    }

    public void onResume() {
        if (oldTheme != -1 || oldLanguage != -1) {
            var themeChanged = oldTheme != -1 && oldTheme != settings.getTheme();
            var languageChanged =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && oldLanguage != -1
                && oldLanguage != settings.getLanguage();
            if (themeChanged || languageChanged) {
                ((android.app.Activity)getContext()).recreate();
            }
            oldTheme = -1;
            oldLanguage = -1;
        }
    }

    @Override
    public void render() {
        var context = getContext();
        var tabs = new int[] {
            R.string.tab_top, R.string.tab_new, R.string.tab_best,
            R.string.tab_ask, R.string.tab_show, R.string.tab_jobs
        };

        new Column(() -> {
            new Row(() -> {
                new Text(R.string.app_name).modifier(actionBarTitle());
                new ImageButton(R.drawable.ic_refresh)
                    .onClick(this::refreshStories)
                    .modifier(actionBarIconButton());
                new ImageButton(R.drawable.ic_dots_vertical)
                    .onClick(v -> new PopupMenu(v.getContext(), v)
                        .item(R.string.menu_options_settings, () -> {
                            oldTheme = settings.getTheme();
                            oldLanguage = settings.getLanguage();
                            openSettings.run();
                        })
                        .show())
                    .modifier(actionBarIconButton());
            }).modifier(actionBar());

            if (state().hasError && state().stories.isEmpty()) {
                renderDisconnected();
            } else {
                new LazyColumn<>(state().stories, s -> s.id() + "_" + s.isLoadMore(),
                    story -> new StoryItem(story, () -> {
                        if (story.isLoadMore()) {
                            loadNextPage();
                        } else {
                            openStory.accept(story.id());
                        }
                    }))
                    .modifier(Modifier.of().width(matchParent()).weight(1));
            }

            new Row(() -> {
                for (var i = 0; i < tabs.length; i++) {
                    var tabIndex = i;
                    var selected = selectedTab == i;
                    new Column(() -> {
                        new Image(TAB_ICONS[tabIndex]).modifier(bottomBarItemIcon());
                        new Text(tabs[tabIndex]).modifier(bottomBarItemLabel());
                    }).modifier(bottomBarItem(selected))
                        .onClick(() -> selectTab(tabIndex));
                }
            }).modifier(bottomBar());
        }).modifier(Modifier.of().width(matchParent()).height(matchParent()));
    }

    private void renderDisconnected() {
        Styles.renderDisconnected(this::refreshStories);
    }

    @Override
    protected void onMount() {
        loadStories();
    }

    private void selectTab(int tab) {
        if (selectedTab == tab)
            return;
        selectedTab = tab;
        rebuild();
        // Only load if this tab has never been loaded
        if (tabStates[tab] == null || state().stories.isEmpty()) {
            loadStories();
        }
    }

    private void refreshStories() {
        cancelCurrentTabLoads();
        tabStates[selectedTab] = new TabState();
        loadStories();
    }

    private void cancelCurrentTabLoads() {
        var s = state();
        if (s.idsTask != null) {
            s.idsTask.cancel();
            s.idsTask = null;
        }
        for (var task : s.pendingTasks) task.cancel();
        s.pendingTasks.clear();
    }

    private void loadStories() {
        var s = state();
        var thisTab = selectedTab;
        s.isLoading = true;
        for (var i = 0; i < PAGE_SIZE; i++) s.stories.add(Story.placeholder(i + 1));
        rebuild();

        var url = "https://hacker-news.firebaseio.com/v0/" + TAB_ENDPOINTS[selectedTab] + ".json";
        s.idsTask = FetchDataTask.with(getContext())
                       .load(url)
                       .then(
                           data -> {
                               try {
                                   var jsonArray = new JSONArray(new String(data, StandardCharsets.UTF_8));
                                   s.storyIds = new int[jsonArray.length()];
                                   for (var i = 0; i < jsonArray.length(); i++) s.storyIds[i] = jsonArray.getInt(i);
                                   s.stories = new ArrayList<>();
                                   s.loadedCount = 0;
                                   loadNextPage(s, thisTab);
                               } catch (JSONException e) {
                                   Log.e(getContext().getPackageName(), "Can't parse story IDs", e);
                               }
                           },
                           e -> {
                               Log.e(getContext().getPackageName(), "Can't fetch story IDs", e);
                               s.hasError = true;
                               s.stories = new ArrayList<>();
                               s.isLoading = false;
                               if (selectedTab == thisTab) rebuild();
                           })
                       .fetch();
    }

    private void loadNextPage() {
        loadNextPage(state(), selectedTab);
    }

    private void loadNextPage(TabState s, int thisTab) {
        var start = s.loadedCount;
        var end = Math.min(start + PAGE_SIZE, s.storyIds.length);
        if (start >= s.storyIds.length)
            return;

        s.stories.removeIf(Story::isLoadMore);
        for (var i = start; i < end; i++) s.stories.add(Story.placeholder(i + 1));
        if (selectedTab == thisTab) rebuild();

        var pendingInPage = new int[] {end - start};
        final var batchTasks = new ArrayList<FetchDataTask>();
        for (var i = start; i < end; i++) {
            final int rank = i + 1;
            final int storyId = s.storyIds[i];
            var task = FetchDataTask.with(getContext())
                           .load("https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json")
                           .then(
                               data -> {
                                   try {
                                       var json = new JSONObject(new String(data, StandardCharsets.UTF_8));
                                       var story = Story.fromJSON(json, rank);
                                       // Find and replace the matching placeholder by rank
                                       for (var j = 0; j < s.stories.size(); j++) {
                                           if (s.stories.get(j).isPlaceholder() && s.stories.get(j).rank() == rank) {
                                               s.stories.set(j, story);
                                               break;
                                           }
                                       }
                                   } catch (JSONException e) {
                                       Log.e(getContext().getPackageName(), "Can't parse story", e);
                                   }
                                   pendingInPage[0]--;
                                   if (pendingInPage[0] == 0) {
                                       s.stories.removeIf(Story::isPlaceholder);
                                       if (end < s.storyIds.length) s.stories.add(Story.LOAD_MORE);
                                       s.isLoading = false;
                                       s.pendingTasks.removeAll(batchTasks);
                                   }
                                   if (selectedTab == thisTab) rebuild();
                               },
                               e -> {
                                   Log.e(getContext().getPackageName(), "Can't fetch story", e);
                                   pendingInPage[0]--;
                                   if (pendingInPage[0] == 0) {
                                       s.stories.removeIf(Story::isPlaceholder);
                                       if (end < s.storyIds.length) s.stories.add(Story.LOAD_MORE);
                                       s.isLoading = false;
                                       s.pendingTasks.removeAll(batchTasks);
                                   }
                                   if (selectedTab == thisTab) rebuild();
                               })
                           .fetch();
            batchTasks.add(task);
            s.pendingTasks.add(task);
        }
        s.loadedCount = end;
    }
}
