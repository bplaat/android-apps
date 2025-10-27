/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

public class Column extends Box {
    public Column(Modifier modifier, OnChildren onChildren) {
        super(Box.Orientation.VERTICAL, modifier, onChildren);
    }

    public Column(OnChildren onChildren) {
        this(null, onChildren);
    }
}
