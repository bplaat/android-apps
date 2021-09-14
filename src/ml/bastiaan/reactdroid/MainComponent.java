package ml.bastiaan.reactdroid;

import android.view.View;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import ml.bastiaan.component.AbsComponent;
import ml.bastiaan.component.Button;
import ml.bastiaan.component.Column;
import ml.bastiaan.component.ComponentContext;
import ml.bastiaan.component.Text;
import ml.bastiaan.component.VerticalScroll;

public class MainComponent extends AbsComponent {
    protected List<Person> persons;

    protected MainComponent(ComponentContext context) {
        super(context);

        persons = new ArrayList<Person>();
        persons.add(new Person("Willem", 51));
        persons.add(new Person("Wietske", 47));
        persons.add(new Person("Bastiaan", 19));
        persons.add(new Person("Sander", 17));
        persons.add(new Person("Leonard", 14));
        persons.add(new Person("Jiska", 13));
    }

    public static MainComponent create(ComponentContext context) {
        return new MainComponent(context);
    }

    public View view() {
        return VerticalScroll.create(context)
            .child(Column.create(context)
                .child(
                    Text.create(context)
                        .text("ReactDroid")
                        .fontSizeSp(16)
                        .fontWeight(500)
                        .paddingDp(16)
                )
                .child(Button.create(context).text("Add person").onClick(view -> {
                    persons.add(new Person("Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                    refresh();
                }))
                .children(
                    persons.stream().map(person -> PersonItem.create(context).person(person))
                        .collect(Collectors.toList())
                )
                .child(
                    Text.create(context)
                        .text("Made by Bastiaan van der Plaat")
                        .textColorRes(R.color.secondary_text_color)
                        .paddingDp(16)
                )
            )
            .build();
    }
}
