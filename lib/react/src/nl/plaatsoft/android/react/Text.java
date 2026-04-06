/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Text {
    private static class TextLayout extends TextView {
        TextLayout(Context context) {
            super(context);
        }
    }

    private final TextLayout ref;

    public Text(String text) {
        var c = BuildContext.current();
        ref = c.slot(TextLayout.class, () -> new TextLayout(c.getContext()));
        if (!text.equals(ref.getText().toString())) {
            ref.setText(text);
        }
    }

    public Text(int textRes) {
        this(BuildContext.current().getContext().getString(textRes));
    }

    public Text modifier(Modifier modifier) {
        modifier.applyToTextView(ref);
        return this;
    }

    public Text onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Text onClick(OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
