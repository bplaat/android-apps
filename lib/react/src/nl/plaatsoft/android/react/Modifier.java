/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import nl.plaatsoft.android.compat.ContextCompat;

/// A pure data builder for visual and layout properties.
/// Start every chain with Modifier.of() and set properties via instance methods.
/// Raw values (dp, sp, resource IDs) are stored without conversion; conversion
/// happens at apply-time using the View's Context.
public class Modifier {
    // Padding (dp), -1 = not set
    private float paddingTopDp = -1;
    private float paddingRightDp = -1;
    private float paddingBottomDp = -1;
    private float paddingLeftDp = -1;

    // Margin (dp), -1 = not set
    private float marginTopDp = -1;
    private float marginRightDp = -1;
    private float marginBottomDp = -1;
    private float marginLeftDp = -1;

    // Layout weight
    private float weight = -1;

    // Size: Float.NaN = not set; -1f = MATCH_PARENT; -2f = WRAP_CONTENT; >= 0 = fixed dp
    private float widthDp = Float.NaN;
    private float heightDp = Float.NaN;

    // Align within parent (Gravity constants; 0 = Gravity.NO_GRAVITY = not set)
    private int gravity = 0;

    // Absolute position inside a Box in dp; Float.NaN = not set
    private float positionXDp = Float.NaN;
    private float positionYDp = Float.NaN;

    // Background; 0 = not set
    private int backgroundResId = 0;
    private int backgroundColor = 0;
    private boolean hasBackground = false;

    // Elevation (dp), -1 = not set
    private float elevationDp = -1;

    // Text
    private float fontSizeSp = -1;
    private int fontWeight = -1;
    private int textColorRes = 0; // 0 = not set
    private int textColor = 0;
    private boolean hasTextColor = false;

    // Scroll
    private boolean scrollVertical = false;
    private boolean scrollHorizontal = false;

    private Modifier() {}

    public static Modifier of() {
        return new Modifier();
    }

    // MARK: Builder methods

    public Modifier paddingDp(float dp) {
        paddingTopDp = paddingRightDp = paddingBottomDp = paddingLeftDp = dp;
        return this;
    }

    public Modifier paddingDp(float vertical, float horizontal) {
        paddingTopDp = paddingBottomDp = vertical;
        paddingRightDp = paddingLeftDp = horizontal;
        return this;
    }

    public Modifier paddingDp(float top, float right, float bottom, float left) {
        paddingTopDp = top;
        paddingRightDp = right;
        paddingBottomDp = bottom;
        paddingLeftDp = left;
        return this;
    }

    public Modifier marginDp(float dp) {
        marginTopDp = marginRightDp = marginBottomDp = marginLeftDp = dp;
        return this;
    }

    public Modifier marginDp(float vertical, float horizontal) {
        marginTopDp = marginBottomDp = vertical;
        marginRightDp = marginLeftDp = horizontal;
        return this;
    }

    public Modifier marginDp(float top, float right, float bottom, float left) {
        marginTopDp = top;
        marginRightDp = right;
        marginBottomDp = bottom;
        marginLeftDp = left;
        return this;
    }

    public Modifier fillHeight() {
        weight = 1f;
        return this;
    }

    public Modifier weight(float w) {
        weight = w;
        return this;
    }

    public Modifier fillMaxWidth() {
        widthDp = ViewGroup.LayoutParams.MATCH_PARENT;
        return this;
    }

    public Modifier fillMaxHeight() {
        heightDp = ViewGroup.LayoutParams.MATCH_PARENT;
        return this;
    }

    public Modifier fillMaxSize() {
        widthDp = heightDp = ViewGroup.LayoutParams.MATCH_PARENT;
        return this;
    }

    public Modifier wrapContentWidth() {
        widthDp = ViewGroup.LayoutParams.WRAP_CONTENT;
        return this;
    }

    public Modifier wrapContentHeight() {
        heightDp = ViewGroup.LayoutParams.WRAP_CONTENT;
        return this;
    }

    public Modifier wrapContentSize() {
        widthDp = heightDp = ViewGroup.LayoutParams.WRAP_CONTENT;
        return this;
    }

    public Modifier width(float dp) {
        widthDp = dp;
        return this;
    }

    public Modifier height(float dp) {
        heightDp = dp;
        return this;
    }

    public Modifier size(float dp) {
        widthDp = heightDp = dp;
        return this;
    }

    public Modifier size(float widthDpVal, float heightDpVal) {
        widthDp = widthDpVal;
        heightDp = heightDpVal;
        return this;
    }

    public Modifier align(int g) {
        gravity = g;
        return this;
    }

    public Modifier position(float xDp, float yDp) {
        positionXDp = xDp;
        positionYDp = yDp;
        return this;
    }

    public Modifier background(int color) {
        backgroundColor = color;
        hasBackground = true;
        return this;
    }

    public Modifier backgroundRes(int res) {
        backgroundResId = res;
        return this;
    }

    public Modifier elevation(float dp) {
        elevationDp = dp;
        return this;
    }

    public Modifier fontSizeSp(float sp) {
        fontSizeSp = sp;
        return this;
    }

    public Modifier fontWeight(int w) {
        fontWeight = w;
        return this;
    }

    public Modifier textColorRes(int res) {
        textColorRes = res;
        return this;
    }

    public Modifier textColor(int color) {
        textColor = color;
        hasTextColor = true;
        return this;
    }

    public Modifier scrollVertical() {
        scrollVertical = true;
        return this;
    }

    public Modifier scrollHorizontal() {
        scrollHorizontal = true;
        return this;
    }

    boolean isScrollVertical() {
        return scrollVertical;
    }
    boolean isScrollHorizontal() {
        return scrollHorizontal;
    }

    // MARK: Apply helpers

    /// Apply visual properties (padding, background, elevation, aspect ratio) to any View.
    public void applyTo(View v) {
        if (paddingTopDp >= 0) {
            var ctx = v.getContext();
            v.setPadding(dpToPx(ctx, paddingLeftDp), dpToPx(ctx, paddingTopDp), dpToPx(ctx, paddingRightDp),
                dpToPx(ctx, paddingBottomDp));
        }
        if (backgroundResId != 0) {
            v.setBackgroundResource(backgroundResId);
        } else if (hasBackground) {
            v.setBackgroundColor(backgroundColor);
        }
        if (elevationDp >= 0) {
            v.setElevation(elevationDp * v.getContext().getResources().getDisplayMetrics().density);
        }
    }

    /// Apply padding + text-specific properties to a TextView.
    public void applyToTextView(TextView tv) {
        applyTo(tv);
        if (fontSizeSp >= 0) {
            tv.setTextSize(fontSizeSp);
        }
        if (fontWeight == 400) {
            tv.setTypeface(null, Typeface.NORMAL);
        } else if (fontWeight == 500) {
            tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else if (fontWeight == 700) {
            tv.setTypeface(null, Typeface.BOLD);
        }
        if (textColorRes != 0) {
            tv.setTextColor(ContextCompat.getColor(tv.getContext(), textColorRes));
        }
        if (hasTextColor) {
            tv.setTextColor(textColor);
        }
        applyLayoutTo(tv);
    }

    /// Apply size, weight, gravity and position LayoutParams to a View.
    /// Parent type is detected at runtime to produce the correct LayoutParams subclass.
    public void applyLayoutTo(View v) {
        var hasWidth = !Float.isNaN(widthDp);
        var hasHeight = !Float.isNaN(heightDp);
        var hasWeight = weight >= 0;
        var hasGravity = gravity != Gravity.NO_GRAVITY;
        var hasPosX = !Float.isNaN(positionXDp);
        var hasPosY = !Float.isNaN(positionYDp);
        var hasMargin = marginTopDp >= 0 || marginRightDp >= 0 || marginBottomDp >= 0 || marginLeftDp >= 0;
        if (!hasWidth && !hasHeight && !hasWeight && !hasGravity && !hasPosX && !hasPosY && !hasMargin)
            return;
        if (hasWeight) {
            // In a horizontal LinearLayout weight distributes width → w=0, h=WRAP_CONTENT.
            // In a vertical LinearLayout weight distributes height → w=MATCH_PARENT, h=0.
            var parent = v.getParent();
            boolean horizontal =
                parent instanceof LinearLayout && ((LinearLayout)parent).getOrientation() == LinearLayout.HORIZONTAL;
            var w = hasWidth ? resolveDim(v, widthDp) : (horizontal ? 0 : LinearLayout.LayoutParams.MATCH_PARENT);
            var h = hasHeight ? resolveDim(v, heightDp) : (horizontal ? LinearLayout.LayoutParams.WRAP_CONTENT : 0);
            var lp = new LinearLayout.LayoutParams(w, h, weight);
            if (hasGravity)
                lp.gravity = gravity;
            if (hasMargin)
                applyMargins(lp, v);
            v.setLayoutParams(lp);
        } else if (v.getParent() instanceof LinearLayout) {
            var w = hasWidth ? resolveDim(v, widthDp) : LinearLayout.LayoutParams.WRAP_CONTENT;
            var h = hasHeight ? resolveDim(v, heightDp) : LinearLayout.LayoutParams.WRAP_CONTENT;
            var lp = new LinearLayout.LayoutParams(w, h);
            if (hasGravity)
                lp.gravity = gravity;
            if (hasMargin)
                applyMargins(lp, v);
            v.setLayoutParams(lp);
        } else {
            // FrameLayout parent (Box) or unknown — use FrameLayout.LayoutParams for gravity + position
            var w = hasWidth ? resolveDim(v, widthDp) : FrameLayout.LayoutParams.WRAP_CONTENT;
            var h = hasHeight ? resolveDim(v, heightDp) : FrameLayout.LayoutParams.WRAP_CONTENT;
            var lp = new FrameLayout.LayoutParams(w, h);
            if (hasGravity)
                lp.gravity = gravity;
            if (hasPosX)
                lp.leftMargin = resolveDim(v, positionXDp);
            if (hasPosY)
                lp.topMargin = resolveDim(v, positionYDp);
            if (hasMargin)
                applyMargins(lp, v);
            v.setLayoutParams(lp);
        }
    }

    private void applyMargins(android.view.ViewGroup.MarginLayoutParams lp, android.view.View v) {
        if (marginTopDp >= 0)
            lp.topMargin = dpToPx(v.getContext(), marginTopDp);
        if (marginRightDp >= 0)
            lp.rightMargin = dpToPx(v.getContext(), marginRightDp);
        if (marginBottomDp >= 0)
            lp.bottomMargin = dpToPx(v.getContext(), marginBottomDp);
        if (marginLeftDp >= 0)
            lp.leftMargin = dpToPx(v.getContext(), marginLeftDp);
    }

    private static int resolveDim(View v, float dp) {
        if (dp < 0)
            return (int)dp; // MATCH_PARENT (-1) or WRAP_CONTENT (-2)
        return dpToPx(v.getContext(), dp);
    }

    private static int dpToPx(Context context, float dp) {
        return (int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
