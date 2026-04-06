/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.view.View.OnClickListener;

public class Button {
    private final android.widget.Button ref;

    public Button(String text) {
        var c = BuildContext.current();
        ref = c.slot(android.widget.Button.class, () -> new android.widget.Button(c.getContext()));
        if (!text.equals(ref.getText().toString()))
            ref.setText(text);
    }

    public Button(int textRes) {
        this(BuildContext.current().getContext().getString(textRes));
    }

    public Button modifier(Modifier modifier) {
        modifier.applyToTextView(ref);
        return this;
    }

    public Button onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Button onClick(OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
