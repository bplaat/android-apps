/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import static nl.plaatsoft.android.react.Unit.*;

import android.view.Gravity;

import nl.plaatsoft.android.react.Modifier;

import ml.coinlist.android.R;

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
            .align(Gravity.CENTER_VERTICAL);
    }

    public static Modifier actionBarIconButton() {
        return Modifier.of().size(dp(56)).background(R.drawable.app_bar_icon_button_ripple);
    }

    // MARK: List items
    public static Modifier listItemSubtitle() {
        return Modifier.of().width(matchParent()).minHeight(dp(56)).padding(dp(16)).fontSize(sp(16)).fontWeight(500);
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
