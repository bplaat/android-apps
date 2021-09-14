package ml.bastiaan.component;

import android.view.View;
import android.widget.LinearLayout;

public class Row extends AbsContainer {
    protected Row(ComponentContext context) {
        super(context);
    }

    public static Row create(ComponentContext context) {
        return new Row(context);
    }

    public LinearLayout view() {
        LinearLayout linearLayout = new LinearLayout(context.getContext());
        linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        for (AbsComponent child : children) {
            linearLayout.addView(child.build());
        }
        return linearLayout;
    }
}
