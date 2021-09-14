package ml.bastiaan.component;

import android.view.View;
import android.widget.ScrollView;

public class VerticalScroll extends Component {
    protected AbsComponent child;

    protected VerticalScroll(ComponentContext context) {
        super(context);
    }

    public static VerticalScroll create(ComponentContext context) {
        return new VerticalScroll(context);
    }

    public VerticalScroll child(AbsComponent child) {
        this.child = child;
        return this;
    }

    public View build() {
        ScrollView scrollView = new ScrollView(context.getContext());
        scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (child != null) scrollView.addView(child.build());
        view = (View)scrollView;
        return view;
    }
}
