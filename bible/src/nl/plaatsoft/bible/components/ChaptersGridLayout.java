/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class ChaptersGridLayout extends ViewGroup {
    private static int COLUMN_COUNT = 6;

    public ChaptersGridLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        var width = MeasureSpec.getSize(widthMeasureSpec);
        var childSize = (width - getPaddingLeft() - getPaddingRight()) / COLUMN_COUNT;
        var childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            child.measure(childMeasureSpec, childMeasureSpec);
        }
        var rowCount = (int)Math.ceil((double)getChildCount() / COLUMN_COUNT);
        var height = rowCount * childSize + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        var x = getPaddingLeft();
        var y = getPaddingTop();
        var width = right - left - getPaddingLeft() - getPaddingRight();
        for (var i = 0; i < getChildCount(); i++) {
            var child = getChildAt(i);
            var childSize = width / COLUMN_COUNT;
            child.layout(x, y, x + childSize, y + childSize);
            x += childSize;
            if (x >= width) {
                x = getPaddingLeft();
                y += childSize;
            }
        }
    }
}
