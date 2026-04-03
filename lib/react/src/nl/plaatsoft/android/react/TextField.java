/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.function.Consumer;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class TextField {
    // Watcher stored in the view tag to survive re-renders without accumulating listeners
    private static class ChangeWatcher implements TextWatcher {
        Consumer<String> callback;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (callback != null)
                callback.accept(s.toString());
        }
    }

    private final EditText et_ref;

    public TextField(String value, Consumer<String> onChange) {
        var c = BuildContext.current();
        var et = c.slot(EditText.class, () -> new EditText(c.getContext()));
        et_ref = et;
        // Only update text if changed to avoid resetting cursor position
        if (!et.getText().toString().equals(value)) {
            et.setText(value);
            et.setSelection(value.length());
        }
        // Reuse watcher across re-renders; only add it once
        var watcher = (ChangeWatcher)et.getTag();
        if (watcher == null) {
            watcher = new ChangeWatcher();
            et.addTextChangedListener(watcher);
            et.setTag(watcher);
        }
        watcher.callback = onChange;
    }

    public TextField modifier(Modifier modifier) {
        modifier.applyTo(et_ref);
        modifier.applyLayoutTo(et_ref);
        return this;
    }
}
