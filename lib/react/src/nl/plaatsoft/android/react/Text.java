/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.widget.TextView;

public class Text {
    private final TextView tv;

    public Text(String text) {
        var c = BuildContext.current();
        tv = c.slot(TextView.class, () -> new TextView(c.getContext()));
        if (!tv.getText().equals(text)) {
            tv.setText(text);
        }
    }

    public Text(int textRes) {
        var c = BuildContext.current();
        tv = c.slot(TextView.class, () -> new TextView(c.getContext()));
        tv.setText(textRes);
    }

    public Text(Object key, String text) {
        var c = BuildContext.current();
        tv = c.slot(key, TextView.class, () -> new TextView(c.getContext()));
        if (!tv.getText().equals(text)) {
            tv.setText(text);
        }
    }

    public Text(Object key, int textRes) {
        var c = BuildContext.current();
        tv = c.slot(key, TextView.class, () -> new TextView(c.getContext()));
        tv.setText(textRes);
    }

    public Text modifier(Modifier modifier) {
        modifier.applyToTextView(tv);
        return this;
    }
}
