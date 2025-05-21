/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import javax.annotation.Nullable;

import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

public class DrawerLayout extends ViewGroup implements View.OnClickListener {
    public static interface OnCloseListener {
        void onClose();
    }

    private static int ANIMATION_DURATION = 150;

    private boolean isOpen = false;
    private boolean isFirstLayout = true;
    private @Nullable OnCloseListener onCloseListener;

    public DrawerLayout(Context context) {
        this(context, null);
    }

    @SuppressWarnings("this-escape")
    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var width = MeasureSpec.getSize(widthMeasureSpec);
        var density = getResources().getDisplayMetrics().density;
        var drawerWidth = Math.min((int) (480 * density), width - (int) (56 * density));
        var child = getChildAt(0);
        child.measure(MeasureSpec.makeMeasureSpec(drawerWidth, MeasureSpec.getMode(widthMeasureSpec)),
                heightMeasureSpec);
        setMeasuredDimension(width, child.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        var child = getChildAt(0);
        child.layout(left, top, child.getMeasuredWidth(), child.getMeasuredHeight());
        if (isFirstLayout) {
            isFirstLayout = false;
            child.setTranslationX(-child.getMeasuredWidth());
        }
    }

    @Override
    public void onClick(@Nullable View v) {
        close();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOnCloseListener(OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public void open() {
        isOpen = true;
        setVisibility(View.VISIBLE);
        post(() -> {
            var colorAnimation = ValueAnimator.ofArgb(Color.TRANSPARENT,
                    Utils.contextGetColor(getContext(), R.color.drawer_overlay_background_color));
            colorAnimation.setDuration(ANIMATION_DURATION);
            colorAnimation.setInterpolator(new DecelerateInterpolator());
            colorAnimation.addUpdateListener(animator -> setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimation.start();

            var child = getChildAt(0);
            child.animate()
                    .translationX(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });
    }

    public void close() {
        isOpen = false;
        var colorAnimation = ValueAnimator.ofArgb(
                Utils.contextGetColor(getContext(), R.color.drawer_overlay_background_color),
                Color.TRANSPARENT);
        colorAnimation.setDuration(ANIMATION_DURATION);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.addUpdateListener(animator -> setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();

        var child = getChildAt(0);
        child.animate()
                .translationX(-child.getWidth())
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> setVisibility(View.GONE))
                .start();

        if (onCloseListener != null)
            onCloseListener.onClose();
    }
}
