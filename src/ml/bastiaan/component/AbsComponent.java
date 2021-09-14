package ml.bastiaan.component;

import android.view.View;
import android.view.ViewGroup;

public abstract class AbsComponent {
    protected ComponentContext context;
    protected View view;

    protected AbsComponent(ComponentContext context) {
        this.context = context;
    }

    public abstract View view();

    public View build() {
        view = view();
        return view;
    }

    public void refresh() {
        ViewGroup parent = (ViewGroup)view.getParent();
        int index = parent.indexOfChild(view);
        parent.removeView(view);
        parent.addView(build(), index);
    }
}
