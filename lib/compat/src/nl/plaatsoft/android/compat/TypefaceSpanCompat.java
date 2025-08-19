/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpanCompat extends MetricAffectingSpan {
    private final Typeface typeface;

    public TypefaceSpanCompat(Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void updateMeasureState(@SuppressWarnings("null") TextPaint ds) {
        applyTypeface(ds);
    }

    @Override
    public void updateDrawState(@SuppressWarnings("null") TextPaint ds) {
        applyTypeface(ds);
    }

    private void applyTypeface(TextPaint paint) {
        paint.setTypeface(typeface);
    }
}
