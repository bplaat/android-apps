package ml.bastiaan.widgets;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

abstract public class Widget {
    protected WidgetContext context;
    protected long key;
    protected int paddingTop;
    protected int paddingRight;
    protected int paddingBottom;
    protected int paddingLeft;

    protected Widget(WidgetContext context) {
        this.context = context;
    }

    public Widget key(long key) {
        this.key = key;
        return this;
    }

    public Widget paddingDp(float padding) {
        paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, context.getContext().getResources().getDisplayMetrics());
        paddingRight = paddingTop;
        paddingBottom = paddingTop;
        paddingLeft = paddingTop;
        return this;
    }

    public Widget paddingDp(float paddingVertical, float paddingHorizontal) {
        paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingVertical, context.getContext().getResources().getDisplayMetrics());
        paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingHorizontal, context.getContext().getResources().getDisplayMetrics());
        paddingBottom = paddingTop;
        paddingLeft = paddingRight;
        return this;
    }

    public Widget paddingDp(float paddingTop, float paddingRight, float paddingBottom) {
        this.paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingTop, context.getContext().getResources().getDisplayMetrics());
        this.paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingRight, context.getContext().getResources().getDisplayMetrics());
        this.paddingBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingBottom, context.getContext().getResources().getDisplayMetrics());
        paddingLeft = this.paddingRight;
        return this;
    }

    public Widget paddingDp(float paddingTop, float paddingRight, float paddingBottom, float paddingLeft) {
        this.paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingTop, context.getContext().getResources().getDisplayMetrics());
        this.paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingRight, context.getContext().getResources().getDisplayMetrics());
        this.paddingBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingBottom, context.getContext().getResources().getDisplayMetrics());
        this.paddingLeft = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingLeft, context.getContext().getResources().getDisplayMetrics());
        return this;
    }

    abstract public View render(ViewGroup parent, View view);
}
