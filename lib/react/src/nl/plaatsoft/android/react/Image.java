/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.widget.ImageView;

public class Image {
    private static final int TAG_DRAWABLE_RES = 0x696d6167; // 'imag'

    private final ImageView ref;

    public Image(int drawableRes) {
        var c = BuildContext.current();
        ref = c.slot(ImageView.class, () -> new ImageView(c.getContext()));
        var last = (Integer)ref.getTag(TAG_DRAWABLE_RES);
        if (last == null || last != drawableRes) {
            ref.setImageResource(drawableRes);
            ref.setTag(TAG_DRAWABLE_RES, drawableRes);
        }
    }

    public Image modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        return this;
    }
}
