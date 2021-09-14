package ml.bastiaan.component;

import android.view.View;
import android.widget.LinearLayout;

public class Column extends AbsContainer {
    protected Column(ComponentContext context) {
        super(context);
    }

    public static Column create(ComponentContext context) {
        return new Column(context);
    }

    public LinearLayout view() {
        LinearLayout linearLayout = new LinearLayout(context.getContext());
        linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (AbsComponent child : children) {
            linearLayout.addView(child.build());
        }
        return linearLayout;
    }
}
