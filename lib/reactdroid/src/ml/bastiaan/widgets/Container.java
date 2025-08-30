package ml.bastiaan.widgets;

import java.util.ArrayList;
import java.util.List;

abstract public class Container extends Widget {
    protected List<Widget> children;

    protected Container(WidgetContext context) {
        super(context);
        children = new ArrayList<Widget>();
    }

    abstract public Container child(Widget child);

    abstract public Container child(List<Widget> children);
}
