package ml.bastiaan.component;

import java.util.ArrayList;

public abstract class Container extends ViewComponent {
    protected ArrayList<Component> children;

    protected Container(ComponentContext context) {
        super(context);
        children = new ArrayList<Component>();
    }

    public Container child(Component child) {
        children.add(child);
        return this;
    }
}
