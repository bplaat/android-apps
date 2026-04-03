/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

public class ImageButton {
    // Distinct subclass so slot() can tell ImageButton apart from other android.widget.ImageButton instances
    private static class ImageButtonView extends android.widget.ImageButton {
        ImageButtonView(android.content.Context context) {
            super(context);
        }
    }

    private final ImageButtonView ib_ref;

    public ImageButton(int drawableRes, Runnable onClick) {
        var c = BuildContext.current();
        var ib = c.slot(ImageButtonView.class, () -> new ImageButtonView(c.getContext()));
        ib_ref = ib;
        ib.setImageResource(drawableRes);
        ib.setOnClickListener(v -> onClick.run());
    }

    public ImageButton(Object key, int drawableRes, Runnable onClick) {
        var c = BuildContext.current();
        var ib = c.slot(key, ImageButtonView.class, () -> new ImageButtonView(c.getContext()));
        ib_ref = ib;
        ib.setImageResource(drawableRes);
        ib.setOnClickListener(v -> onClick.run());
    }

    public ImageButton modifier(Modifier modifier) {
        modifier.applyTo(ib_ref);
        modifier.applyLayoutTo(ib_ref);
        return this;
    }
}
