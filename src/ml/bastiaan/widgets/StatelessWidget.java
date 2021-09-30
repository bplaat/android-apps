package ml.bastiaan.widgets;

import android.view.View;
import android.view.ViewGroup;

abstract public class StatelessWidget extends Widget {
    public StatelessWidget(WidgetContext context) {
        super(context);
    }

    abstract public Widget build();

    public View render(ViewGroup parent, View view) {
        return build().render(parent, view);
    }
}
