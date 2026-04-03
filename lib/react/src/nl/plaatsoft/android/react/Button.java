/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

public class Button {
    private final android.widget.Button btn;

    public Button(String label, Runnable onClick) {
        var c = BuildContext.current();
        btn = c.slot(android.widget.Button.class, () -> new android.widget.Button(c.getContext()));
        if (!btn.getText().equals(label)) {
            btn.setText(label);
        }
        btn.setOnClickListener(v -> onClick.run());
    }

    public Button(Object key, String label, Runnable onClick) {
        var c = BuildContext.current();
        btn = c.slot(key, android.widget.Button.class, () -> new android.widget.Button(c.getContext()));
        if (!btn.getText().equals(label)) {
            btn.setText(label);
        }
        btn.setOnClickListener(v -> onClick.run());
    }

    public Button modifier(Modifier modifier) {
        modifier.applyTo(btn);
        modifier.applyLayoutTo(btn);
        return this;
    }
}
