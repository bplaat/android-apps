/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.graphics.Color;
import android.widget.ImageView;

import nl.plaatsoft.android.fetch.FetchImageTask;

import org.jspecify.annotations.Nullable;

public class Image {
    private static final int TAG_DRAWABLE_RES = 0x696d6167; // 'imag'
    private static final int TAG_URL = 0x75726c78; // 'urlx'

    private final ImageView ref;
    private @Nullable String url = null;
    private boolean isTransparent = false;
    private int loadingColorValue = Color.TRANSPARENT;
    private boolean needsFetch = false;

    public Image(int drawableRes) {
        var c = BuildContext.current();
        ref = c.slot(ImageView.class, () -> new ImageView(c.getContext()));
        ref.setTag(TAG_URL, null);
        var last = (Integer)ref.getTag(TAG_DRAWABLE_RES);
        if (last == null || last != drawableRes) {
            ref.setImageResource(drawableRes);
            ref.setTag(TAG_DRAWABLE_RES, drawableRes);
        }
    }

    public Image(String url) {
        var c = BuildContext.current();
        ref = c.slot(ImageView.class, () -> new ImageView(c.getContext()));
        ref.setTag(TAG_DRAWABLE_RES, null);
        this.url = url;
        var lastUrl = (String)ref.getTag(TAG_URL);
        needsFetch = !url.equals(lastUrl);
        if (needsFetch)
            ref.setTag(TAG_URL, url);
    }

    public Image transparent() {
        isTransparent = true;
        return this;
    }

    public Image loadingColor(int color) {
        loadingColorValue = color;
        return this;
    }

    public Image scaleType(ImageView.ScaleType scaleType) {
        if (ref.getScaleType() != scaleType)
            ref.setScaleType(scaleType);
        return this;
    }

    public Image modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        if (url != null)
            applyUrl();
        return this;
    }

    // Call when no modifier is chained.
    public void fetch() {
        if (url != null)
            applyUrl();
    }

    private void applyUrl() {
        if (!needsFetch) {
            // Same URL: restore bitmap if the view was recycled and lost its drawable.
            var cached = FetchImageTask.getCached(url);
            if (cached != null && ref.getDrawable() == null) {
                ref.setImageBitmap(cached);
                ref.setImageAlpha(255);
                if (isTransparent)
                    ref.setBackgroundColor(Color.TRANSPARENT);
            }
            return;
        }
        needsFetch = false;

        // URL changed: serve from memory cache instantly to avoid any flash.
        var cached = FetchImageTask.getCached(url);
        if (cached != null) {
            ref.setImageBitmap(cached);
            ref.setImageAlpha(255);
            if (isTransparent)
                ref.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        // Not cached: show loading state and kick off async fetch.
        ref.setImageBitmap(null);
        ref.setImageAlpha(255);
        if (isTransparent && loadingColorValue != Color.TRANSPARENT)
            ref.setBackgroundColor(loadingColorValue);

        var task = FetchImageTask.with(ref.getContext()).load(url).into(ref);
        if (isTransparent)
            task.transparent();
        if (loadingColorValue != Color.TRANSPARENT)
            task.loadingColor(loadingColorValue);
        task.fetch();
    }
}
