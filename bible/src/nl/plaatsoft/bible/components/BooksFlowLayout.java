/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.components;

import android.content.Context;
import android.view.ViewGroup;

public class BooksFlowLayout extends ViewGroup {
    public BooksFlowLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var width = MeasureSpec.getSize(widthMeasureSpec);
        var x = 0;
        var height = -1;
        var childHeight = 0;
        var childMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            child.measure(childMeasureSpec, childMeasureSpec);
            if (height == -1) {
                childHeight = child.getMeasuredHeight();
                height = childHeight + getPaddingTop() + getPaddingBottom();
            }
            x += child.getMeasuredWidth();
            if (x > (width - getPaddingLeft() - getPaddingRight())) {
                x = 0;
                height += childHeight;
            }
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        var x = getPaddingLeft();
        var y = getPaddingTop();
        var width = right - left - getPaddingLeft() - getPaddingRight();
        var childHeight = getChildAt(0).getMeasuredHeight();
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            if (x + child.getMeasuredWidth() > width) {
                x = getPaddingLeft();
                y += childHeight;
            }
            child.layout(x, y, x + child.getMeasuredWidth(), y + childHeight);
            x += child.getMeasuredWidth();
        }
    }
}
