package ml.bastiaan.widgets;

import java.util.ArrayList;
import java.util.List;

abstract public class Bin extends Widget {
    protected Widget child;

    protected Bin(WidgetContext context) {
        super(context);
    }

    abstract public Bin child(Widget child);
}
