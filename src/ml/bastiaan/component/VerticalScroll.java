package ml.bastiaan.component;

import android.view.View;
import android.widget.ScrollView;

public class VerticalScroll extends AbsBin {
    protected VerticalScroll(ComponentContext context) {
        super(context);
    }

    public static VerticalScroll create(ComponentContext context) {
        return new VerticalScroll(context);
    }

    public ScrollView view() {
        ScrollView scrollView = new ScrollView(context.getContext());
        scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (child != null) scrollView.addView(child.build());
        return scrollView;
    }
}
