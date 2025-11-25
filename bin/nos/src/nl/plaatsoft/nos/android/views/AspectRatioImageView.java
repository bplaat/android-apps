/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import nl.plaatsoft.nos.android.R;

public class AspectRatioImageView extends ImageView {
    private float aspectRatio = 1.0f;

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
            aspectRatio = a.getFloat(R.styleable.AspectRatioImageView_aspectRatio, 1.0f);
            a.recycle();
        }
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio > 0 && this.aspectRatio != aspectRatio) {
            this.aspectRatio = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width;
        int height;

        // If width is EXACTLY and not MATCH_PARENT (i.e., set in dp), don't use aspect ratio
        if (widthMode == MeasureSpec.EXACTLY && getLayoutParams() != null && getLayoutParams().width > 0) {
            width = originalWidth;
            height = MeasureSpec.getSize(heightMeasureSpec);
            if (heightMode != MeasureSpec.EXACTLY) {
                height = (int)(width / aspectRatio);
            }
        } else if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = (int)(height * aspectRatio);
        } else {
            width = originalWidth;
            height = (int)(width / aspectRatio);
        }

        setMeasuredDimension(width, height);
    }
}
