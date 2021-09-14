package ml.bastiaan.component;

import android.view.View;
import android.widget.LinearLayout;

public class Column extends Container {
    protected Column(ComponentContext context) {
        super(context);
    }

    public static Column create(ComponentContext context) {
        return new Column(context);
    }

    public View build() {
        LinearLayout linearLayout = new LinearLayout(context.getContext());
        linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (Component child : children) {
            linearLayout.addView(child.build());
        }
        view = (View)linearLayout;
        return view;
    }
}
