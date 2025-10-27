/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jspecify.annotations.Nullable;

public class Text extends Widget {
    protected String text;

    public Text(String text, Modifier modifier) {
        super(modifier);
        this.text = text;
    }

    public Text(String text) {
        this(text, null);
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        TextView textView;
        if (view != null && view.getClass().equals(TextView.class)) {
            textView = (TextView)view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                textView = new TextView(WidgetContext.getContext());
                parent.addView(textView, index);
            } else {
                textView = new TextView(WidgetContext.getContext());
                parent.addView(textView);
            }
            textView.setTag(key);
        }

        if (modifier != null) {
            modifier.applyTo(textView);
        }

        return textView;
    }
}
