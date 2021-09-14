package ml.bastiaan.component;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsContainer extends Component {
    protected List<AbsComponent> children;

    protected AbsContainer(ComponentContext context) {
        super(context);
        children = new ArrayList<AbsComponent>();
    }

    public AbsContainer child(AbsComponent child) {
        children.add(child);
        return this;
    }

    public AbsContainer children(List<AbsComponent> children) {
        this.children.addAll(children);
        return this;
    }
}
