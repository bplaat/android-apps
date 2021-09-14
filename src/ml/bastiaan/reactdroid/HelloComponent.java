package ml.bastiaan.reactdroid;

import android.view.View;
import ml.bastiaan.component.AbsComponent;
import ml.bastiaan.component.Button;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Text;
import ml.bastiaan.component.Row;

public class HelloComponent extends AbsComponent {
    protected String name;
    protected int years;

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

    public HelloComponent years(int years) {
        this.years = years;
        return this;
    }

    public View view() {
        return Column.create(context)
            .child(Text.create(context).text("Hello " + name + ", i'm " + years + " old!"))
            .child(
                Row.create(context)
                    .child(Button.create(context).text("+").onClick(view -> {
                        this.years++;
                        refresh();
                    }))
                    .child(Button.create(context).text("-").onClick(view -> {
                        this.years--;
                        refresh();
                    }))
            )
            .paddingDp(8, 16)
            .build();
    }
}
