package ml.bastiaan.reactdroid;

import android.view.View;
import ml.bastiaan.component.AbsComponent;
import ml.bastiaan.component.Button;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Text;
import ml.bastiaan.component.Row;

public class PersonItem extends AbsComponent {
    protected Person person;

    protected PersonItem(ComponentContext context) {
        super(context);
    }

    public static PersonItem create(ComponentContext context) {
        return new PersonItem(context);
    }

    public PersonItem person(Person person) {
        this.person = person;
        return this;
    }

    public View view() {
        return Column.create(context)
            .child(Text.create(context).text("Hello " + person.name + ", i'm " + person.age + " old!"))
            .child(
                Row.create(context)
                    .child(Button.create(context).text("+").onClick(view -> {
                        person.age++;
                        refresh();
                    }))
                    .child(Button.create(context).text("-").onClick(view -> {
                        person.age--;
                        refresh();
                    }))
            )
            .paddingDp(8, 16)
            .build();
    }
}
