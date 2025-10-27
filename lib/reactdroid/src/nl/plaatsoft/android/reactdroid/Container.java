/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import java.util.ArrayList;
import java.util.List;

public abstract class Container extends Widget {
    public static interface OnChildren {
        void onChildren();
    }

    protected OnChildren onChildren;

    protected Container(Modifier modifier, OnChildren onChildren) {
        super(modifier);
        this.onChildren = onChildren;
    }

    protected Container(OnChildren onChildren) {
        this(null, onChildren);
    }
}
