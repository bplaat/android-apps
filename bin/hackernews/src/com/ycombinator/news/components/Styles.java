/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;

import android.view.Gravity;

import org.jspecify.annotations.Nullable;

import nl.plaatsoft.android.react.Box;
import nl.plaatsoft.android.react.Column;
import nl.plaatsoft.android.react.Modifier;
import nl.plaatsoft.android.react.Text;

import com.ycombinator.news.R;

public class Styles {
    private Styles() {}

    // MARK: Action bar
    public static Modifier actionBar() {
        return Modifier.of().width(matchParent()).background(R.color.primary_color).elevation(dp(4));
    }

    public static Modifier actionBarTitle() {
        return Modifier.of()
            .weight(1)
            .fontSize(sp(20))
            .fontWeight(Modifier.FontWeight.MEDIUM)
            .paddingX(dp(16))
            .textSingleLine()
            .align(Gravity.CENTER_VERTICAL);
    }

    public static Modifier actionBarIconButton() {
        return Modifier.of().size(dp(56)).background(R.drawable.app_bar_icon_button_ripple);
    }

    // MARK: Disconnected error view
    public static void renderDisconnected(@Nullable Runnable onRetry) {
        new Box(() -> {
            new Column(() -> {
                new Text(R.string.main_disconnected_title_label)
                    .modifier(Modifier.of()
                            .fontSize(sp(24))
                            .fontWeight(Modifier.FontWeight.MEDIUM)
                            .textGravity(Gravity.CENTER_HORIZONTAL)
                            .margin(dp(0), dp(0), dp(8), dp(0)));
                new Text(R.string.main_disconnected_description_label)
                    .modifier(Modifier.of()
                            .textColor(R.color.secondary_text_color)
                            .textGravity(Gravity.CENTER_HORIZONTAL));
                if (onRetry != null) {
                    new Text(R.string.main_disconnected_hero_button)
                        .modifier(Modifier.of()
                                .textColor(R.color.tab_selected_color)
                                .textGravity(Gravity.CENTER_HORIZONTAL)
                                .margin(dp(0), dp(16), dp(0), dp(0))
                                .backgroundAttr(android.R.attr.selectableItemBackgroundBorderless))
                        .onClick(onRetry);
                }
            }).modifier(Modifier.of().align(Gravity.CENTER).padding(dp(32)));
        }).modifier(Modifier.of().width(matchParent()).weight(1));
    }

    // MARK: Bottom bar
    public static Modifier bottomBar() {
        return Modifier.of()
            .width(matchParent())
            .background(R.color.primary_color)
            .elevation(dp(8))
            .useWindowInsets();
    }

    public static Modifier bottomBarItem(boolean selected) {
        return Modifier.of()
            .weight(1)
            .height(dp(56))
            .contentGravity(Gravity.CENTER)
            .backgroundAttr(android.R.attr.selectableItemBackgroundBorderless)
            .alpha(selected ? 1f : 0.6f);
    }

    public static Modifier bottomBarItemIcon() {
        return Modifier.of().size(dp(24));
    }

    public static Modifier bottomBarItemLabel() {
        return Modifier.of()
            .width(matchParent())
            .fontSize(sp(10))
            .textGravity(Gravity.CENTER)
            .textSingleLine();
    }

    // MARK: List items
    public static Modifier listItemSubtitle() {
        return Modifier.of().width(matchParent()).minHeight(dp(56)).padding(dp(16)).fontSize(sp(16)).fontWeight(Modifier.FontWeight.MEDIUM);
    }

    public static Modifier listItemButton() {
        return Modifier.of()
            .width(matchParent())
            .minHeight(dp(56))
            .padding(dp(16))
            .contentGravity(Gravity.CENTER_VERTICAL)
            .backgroundAttr(android.R.attr.selectableItemBackground);
    }

    public static Modifier listItemButtonIcon() {
        return Modifier.of().size(dp(24)).margin(dp(0), dp(32), dp(0), dp(0));
    }

    public static Modifier listItemButtonLabel() {
        return Modifier.of().weight(1).textSingleLine();
    }

    public static Modifier listItemButtonMeta() {
        return Modifier.of().margin(dp(0), dp(0), dp(0), dp(16)).textColor(R.color.secondary_text_color);
    }

    public static Modifier listItemFooter() {
        return Modifier.of()
            .width(matchParent())
            .minHeight(dp(56))
            .padding(dp(16))
            .textColor(R.color.secondary_text_color)
            .textGravity(Gravity.CENTER_HORIZONTAL)
            .backgroundAttr(android.R.attr.selectableItemBackground);
    }
}
