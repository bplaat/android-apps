/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.util.TypedValue;
import android.view.ViewGroup;

public class Unit {
    public static enum Type { PX, DP, SP, MATCH_PARENT, WRAP_CONTENT }

    private final Type type;
    private final float value;

    private Unit(Type type, float value) {
        this.type = type;
        this.value = value;
    }

    public static final Unit px(float value) {
        return new Unit(Type.PX, value);
    }
    public static final Unit dp(float value) {
        return new Unit(Type.DP, value);
    }
    public static final Unit sp(float value) {
        return new Unit(Type.SP, value);
    }
    public static final Unit matchParent() {
        return new Unit(Type.MATCH_PARENT, 0);
    }
    public static final Unit wrapContent() {
        return new Unit(Type.WRAP_CONTENT, 0);
    }

    public Type getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public float resolve(Context context) {
        var dm = context.getResources().getDisplayMetrics();
        return switch (type) {
            case PX -> value;
            case DP -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm);
            case SP -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, dm);
            case MATCH_PARENT -> ViewGroup.LayoutParams.MATCH_PARENT;
            case WRAP_CONTENT -> ViewGroup.LayoutParams.WRAP_CONTENT;
        };
    }
}
