/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import javax.annotation.Nullable;

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var width = MeasureSpec.getSize(widthMeasureSpec);
        var x = 0;
        var rows = 1;
        var childHeight = 0;
        var childMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            child.measure(childMeasureSpec, childMeasureSpec);
            if (i == 0)
                childHeight = child.getMeasuredHeight();
            if (x + child.getMeasuredWidth() >= (width - getPaddingLeft() - getPaddingRight())) {
                x = 0;
                rows++;
            }
            x += child.getMeasuredWidth();
        }
        var height = rows * childHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        var x = getPaddingLeft();
        var y = getPaddingTop();
        var childHeight = getChildAt(0).getMeasuredHeight();
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            if (x + child.getMeasuredWidth() >= (right - left - getPaddingRight())) {
                x = getPaddingLeft();
                y += childHeight;
            }
            child.layout(x, y, x + child.getMeasuredWidth(), y + childHeight);
            x += child.getMeasuredWidth();
        }
    }
}
