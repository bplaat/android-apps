package ml.bastiaan.widgets;

import android.view.View;
import android.view.ViewGroup;

public abstract class StatefulWidget extends Widget {
    protected ViewGroup _parent;
    protected View _view;

    protected StatefulWidget(WidgetContext context) {
        super(context);
    }

    public abstract Widget build();

    public void refresh() {
        build().render(_parent, _view);
    }

    public View render(ViewGroup parent, View view) {
        _parent = parent;
        return _view = build().render(parent, view);
    }
}
