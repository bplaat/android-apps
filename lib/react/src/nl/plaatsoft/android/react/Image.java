/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.widget.ImageView;

public class Image {
    private final ImageView iv_ref;

    public Image(int drawableRes) {
        var c = BuildContext.current();
        var iv = c.slot(ImageView.class, () -> new ImageView(c.getContext()));
        iv_ref = iv;
        iv.setImageResource(drawableRes);
    }

    public Image(Object key, int drawableRes) {
        var c = BuildContext.current();
        var iv = c.slot(key, ImageView.class, () -> new ImageView(c.getContext()));
        iv_ref = iv;
        iv.setImageResource(drawableRes);
    }

    public Image modifier(Modifier modifier) {
        modifier.applyTo(iv_ref);
        modifier.applyLayoutTo(iv_ref);
        return this;
    }
}
