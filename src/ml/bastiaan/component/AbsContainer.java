package ml.bastiaan.component;

import java.util.ArrayList;

public abstract class AbsContainer extends Component {
    protected ArrayList<AbsComponent> children;

    protected AbsContainer(ComponentContext context) {
        super(context);
        children = new ArrayList<AbsComponent>();
    }

    public AbsContainer child(AbsComponent child) {
        children.add(child);
        return this;
    }
}
