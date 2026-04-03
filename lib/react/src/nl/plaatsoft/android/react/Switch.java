/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.function.Consumer;

public class Switch {
    private final android.widget.Switch sw_ref;

    public Switch(boolean checked, Consumer<Boolean> onChange) {
        var c = BuildContext.current();
        var sw = c.slot(android.widget.Switch.class, () -> new android.widget.Switch(c.getContext()));
        sw_ref = sw;
        // Null out listener before setChecked to avoid firing onChange during sync
        sw.setOnCheckedChangeListener(null);
        if (sw.isChecked() != checked) {
            sw.setChecked(checked);
        }
        sw.setOnCheckedChangeListener((btn, isChecked) -> onChange.accept(isChecked));
    }

    public Switch(Object key, boolean checked, Consumer<Boolean> onChange) {
        var c = BuildContext.current();
        var sw = c.slot(key, android.widget.Switch.class, () -> new android.widget.Switch(c.getContext()));
        sw_ref = sw;
        sw.setOnCheckedChangeListener(null);
        if (sw.isChecked() != checked) {
            sw.setChecked(checked);
        }
        sw.setOnCheckedChangeListener((btn, isChecked) -> onChange.accept(isChecked));
    }

    public Switch modifier(Modifier modifier) {
        modifier.applyTo(sw_ref);
        modifier.applyLayoutTo(sw_ref);
        return this;
    }
}
