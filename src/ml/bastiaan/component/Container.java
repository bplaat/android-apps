package ml.bastiaan.component;

import java.util.ArrayList;

public abstract class Container extends Component {
    protected ArrayList<AbsComponent> children;

    protected Container(ComponentContext context) {
        super(context);
        children = new ArrayList<AbsComponent>();
    }

    public Container child(AbsComponent child) {
        children.add(child);
        return this;
    }
}
