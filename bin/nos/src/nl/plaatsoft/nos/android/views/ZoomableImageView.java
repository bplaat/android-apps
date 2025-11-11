/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */
package nl.plaatsoft.nos.android.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class ZoomableImageView extends ImageView {
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private ScaleGestureDetector scaleDetector;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private float minScale = 1f;
    private float maxScale = 5f;
    private float currentScale = 1f;

    private final PointF lastTouch = new PointF();

    private int imageWidth, imageHeight;
    private int viewWidth, viewHeight;

    @SuppressWarnings("this-escape")
    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        resetImage();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        resetImage();
    }

    @Override
    public void setImageDrawable(android.graphics.drawable.Drawable drawable) {
        super.setImageDrawable(drawable);
        resetImage();
    }

    private void resetImage() {
        if (getDrawable() == null)
            return;

        imageWidth = getDrawable().getIntrinsicWidth();
        imageHeight = getDrawable().getIntrinsicHeight();

        matrix.reset();
        centerCropImage();
        currentScale = minScale;
        setImageMatrix(matrix);
    }

    private void centerCropImage() {
        if (imageWidth <= 0 || imageHeight <= 0 || viewWidth <= 0 || viewHeight <= 0)
            return;

        var scaleX = (float)viewWidth / imageWidth;
        var scaleY = (float)viewHeight / imageHeight;
        var scale = Math.min(scaleX, scaleY);

        minScale = scale;

        var dx = (viewWidth - imageWidth * scale) / 2f;
        var dy = (viewHeight - imageHeight * scale) / 2f;

        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        var x = event.getX();
        var y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                lastTouch.set(x, y);
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                savedMatrix.set(matrix);
                lastTouch.set(x, y);
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(x - lastTouch.x, y - lastTouch.y);
                    clampTranslation();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }

        setImageMatrix(matrix);
        return true;
    }

    private void clampTranslation() {
        matrix.getValues(matrixValues);
        var transX = matrixValues[Matrix.MTRANS_X];
        var transY = matrixValues[Matrix.MTRANS_Y];
        var scale = getCurrentScale();

        var drawableW = imageWidth * scale;
        var drawableH = imageHeight * scale;
        var left = viewWidth - drawableW;
        var top = viewHeight - drawableH;

        if (drawableW <= viewWidth) {
            transX = (viewWidth - drawableW) / 2f;
        } else {
            transX = Math.max(Math.min(transX, 0), left);
        }

        if (drawableH <= viewHeight) {
            transY = (viewHeight - drawableH) / 2f;
        } else {
            transY = Math.max(Math.min(transY, 0), top);
        }

        matrixValues[Matrix.MTRANS_X] = transX;
        matrixValues[Matrix.MTRANS_Y] = transY;
        matrix.setValues(matrixValues);
    }

    private float getCurrentScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            var scaleFactor = detector.getScaleFactor();
            var newScale = currentScale * scaleFactor;

            if (newScale >= minScale && newScale <= maxScale) {
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                currentScale = newScale;
                clampTranslation();
            }
            return true;
        }
    }
}
