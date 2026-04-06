/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.view.View.OnClickListener;

public class ImageButton {
    private static class ImageButtonView extends android.widget.ImageButton {
        ImageButtonView(android.content.Context context) {
            super(context);
        }
    }

    private static final int TAG_DRAWABLE_RES = 0x64726177; // 'draw'

    private final ImageButtonView ref;

    public ImageButton(int drawableRes) {
        var c = BuildContext.current();
        ref = c.slot(ImageButtonView.class, () -> new ImageButtonView(c.getContext()));
        var last = (Integer)ref.getTag(TAG_DRAWABLE_RES);
        if (last == null || last != drawableRes) {
            ref.setImageResource(drawableRes);
            ref.setTag(TAG_DRAWABLE_RES, drawableRes);
        }
    }

    public ImageButton modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        return this;
    }

    public ImageButton onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public ImageButton onClick(OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
