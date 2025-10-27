/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

public class Row extends Box {
    public Row(Modifier modifier, OnChildren onChildren) {
        super(Box.Orientation.HORIZONTAL, modifier, onChildren);
    }

    public Row(OnChildren onChildren) {
        this(null, onChildren);
    }
}
