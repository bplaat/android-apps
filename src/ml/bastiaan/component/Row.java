package ml.bastiaan.component;

import android.view.View;
import android.widget.LinearLayout;

public class Row extends Container {
    protected Row(ComponentContext context) {
        super(context);
    }

    public static Row create(ComponentContext context) {
        return new Row(context);
    }

    public View build() {
        LinearLayout linearLayout = new LinearLayout(context.getContext());
        linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        for (AbsComponent child : children) {
            linearLayout.addView(child.build());
        }
        view = (View)linearLayout;
        return view;
    }
}
