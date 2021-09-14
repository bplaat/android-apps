package ml.bastiaan.component;

import java.util.ArrayList;

public abstract class AbsBin extends Component {
    protected AbsComponent child;

    protected AbsBin(ComponentContext context) {
        super(context);
    }

    public AbsBin child(AbsComponent child) {
        this.child = child;
        return this;
    }
}
