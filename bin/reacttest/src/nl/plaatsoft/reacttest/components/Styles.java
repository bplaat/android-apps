/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import static nl.plaatsoft.android.react.Unit.*;

import android.view.Gravity;

import nl.plaatsoft.android.react.*;
import nl.plaatsoft.reacttest.R;

public class Styles {
    private Styles() {}

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
}
