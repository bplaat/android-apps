package ml.bastiaan.component;

import android.view.View;
import android.view.ViewGroup;

public abstract class Component {
    protected ComponentContext context;
    protected View view;

    protected Component(ComponentContext context) {
        this.context = context;
    }

    public abstract View build();

    public void refresh() {
        ViewGroup parent = (ViewGroup)view.getParent();
        int index = parent.indexOfChild(view);
        parent.removeView(view);
        parent.addView(build(), index);
    }
}
