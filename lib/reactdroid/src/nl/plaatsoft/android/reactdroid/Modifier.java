/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class Modifier {
    // View
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private int paddingLeft;
    private int backgroundColor = -1;

    // TextView
    private int textColor = -1;
    private int fontSize = -1;
    private int fontWeight = 400;
    private int textStyle = Typeface.NORMAL;

    public Modifier() {}

    // MARK: View
    public Modifier padding(float padding) {
        paddingTop = Utils.dpToPx(WidgetContext.getContext(), padding);
        paddingRight = paddingTop;
        paddingBottom = paddingTop;
        paddingLeft = paddingTop;
        return this;
    }

    public Modifier padding(float paddingVertical, float paddingHorizontal) {
        paddingTop = Utils.dpToPx(WidgetContext.getContext(), paddingVertical);
        paddingRight = Utils.dpToPx(WidgetContext.getContext(), paddingHorizontal);
        paddingBottom = paddingTop;
        paddingLeft = paddingRight;
        return this;
    }

    public Modifier padding(float paddingTop, float paddingRight, float paddingBottom) {
        this.paddingTop = Utils.dpToPx(WidgetContext.getContext(), paddingTop);
        this.paddingRight = Utils.dpToPx(WidgetContext.getContext(), paddingRight);
        this.paddingBottom = Utils.dpToPx(WidgetContext.getContext(), paddingBottom);
        paddingLeft = this.paddingRight;
        return this;
    }

    public Modifier padding(float paddingTop, float paddingRight, float paddingBottom, float paddingLeft) {
        this.paddingTop = Utils.dpToPx(WidgetContext.getContext(), paddingTop);
        this.paddingRight = Utils.dpToPx(WidgetContext.getContext(), paddingRight);
        this.paddingBottom = Utils.dpToPx(WidgetContext.getContext(), paddingBottom);
        this.paddingLeft = Utils.dpToPx(WidgetContext.getContext(), paddingLeft);
        return this;
    }

    public Modifier backgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    // MARK: TextView
    public Modifier fontSize(int fontSize) {
        this.fontSize = (int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, fontSize, WidgetContext.getContext().getResources().getDisplayMetrics());
        return this;
    }

    public Modifier fontWeight(int fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    }

    @SuppressWarnings("deprecation")
    public Modifier textColorRes(int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textColor = new ContextWrapper(WidgetContext.getContext()).getColor(colorId);
        } else {
            textColor = WidgetContext.getContext().getResources().getColor(colorId);
        }
        return this;
    }

    // MARK: Apply to
    public void applyTo(View view) {
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (backgroundColor != -1) {
            view.setBackgroundColor(backgroundColor);
        }

        if (view instanceof TextView textView) {
            if (textColor != -1) {
                textView.setTextColor(textColor);
            }
            if (fontSize != -1) {
                textView.setTextSize(fontSize);
            }

            var currentTypeface = textView.getTypeface();
            var currentStyle = currentTypeface != null ? currentTypeface.getStyle() : Typeface.NORMAL;
            if ((fontWeight == 700 && currentStyle != Typeface.BOLD)
                || (fontWeight == 400 && currentStyle != Typeface.NORMAL)
                || (fontWeight == 500
                    && (currentTypeface == null
                        || !Typeface.create("sans-serif-medium", Typeface.NORMAL).equals(currentTypeface)))) {
                if (fontWeight == 400)
                    textView.setTypeface(null, Typeface.NORMAL);
                if (fontWeight == 500)
                    textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                if (fontWeight == 700)
                    textView.setTypeface(null, Typeface.BOLD);
            }
        }
    }
}
