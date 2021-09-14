package ml.bastiaan.reactdroid;

import android.view.View;
import ml.bastiaan.component.ButtonComponent;
import ml.bastiaan.component.Component;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.Text;

public class HelloComponent extends Component {
    protected String name;
    protected int counter = 0;

    protected HelloComponent(ComponentContext context) {
        super(context);
    }

    public static HelloComponent create(ComponentContext context) {
        return new HelloComponent(context);
    }

    public HelloComponent name(String name) {
        this.name = name;
        return this;
    }

    public View build() {
        view = Column.create(context)
            .child(Text.create(context).text("Hello " + name + "!"))
            .child(ButtonComponent.create(context).text("Click counter: " + counter).onClick(view -> {
                this.counter++;
                refresh();
            }))
            .paddingDp(8, 16)
            .build();
        return view;
    }
}
