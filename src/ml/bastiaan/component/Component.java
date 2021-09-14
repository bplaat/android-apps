package ml.bastiaan.component;

import android.view.View;
import android.util.TypedValue;

public class Component extends AbsComponent {
    protected int paddingTop;
    protected int paddingRight;
    protected int paddingBottom;
    protected int paddingLeft;

    protected Component(ComponentContext context) {
        super(context);
    }

    public static Component create(ComponentContext context) {
        return new Component(context);
    }

    public Component paddingDp(float padding) {
        paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, context.getContext().getResources().getDisplayMetrics());
        paddingRight = paddingTop;
        paddingBottom = paddingTop;
        paddingLeft = paddingTop;
        return this;
    }

    public Component paddingDp(float paddingVertical, float paddingHorizontal) {
        paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingVertical, context.getContext().getResources().getDisplayMetrics());
        paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingHorizontal, context.getContext().getResources().getDisplayMetrics());
        paddingBottom = paddingTop;
        paddingLeft = paddingRight;
        return this;
    }

    public Component paddingDp(float paddingTop, float paddingRight, float paddingBottom) {
        this.paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingTop, context.getContext().getResources().getDisplayMetrics());
        this.paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingRight, context.getContext().getResources().getDisplayMetrics());
        this.paddingBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingBottom, context.getContext().getResources().getDisplayMetrics());
        paddingLeft = this.paddingRight;
        return this;
    }

    public Component paddingDp(float paddingTop, float paddingRight, float paddingBottom, float paddingLeft) {
        this.paddingTop = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingTop, context.getContext().getResources().getDisplayMetrics());
        this.paddingRight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingRight, context.getContext().getResources().getDisplayMetrics());
        this.paddingBottom = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingBottom, context.getContext().getResources().getDisplayMetrics());
        this.paddingLeft = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingLeft, context.getContext().getResources().getDisplayMetrics());
        return this;
    }

    public View build() {
        view = new View(context.getContext());
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        return view;
    }
}
