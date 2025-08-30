package ml.bastiaan.reactdroid;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import ml.bastiaan.widgets.*;

public class HomeScreen extends StatefulWidget {
    protected List<Person> persons;

    protected HomeScreen(WidgetContext context) {
        super(context);

        persons = new ArrayList<Person>();
        persons.add(new Person(1, "Willem", 51));
        persons.add(new Person(2, "Wietske", 47));
        persons.add(new Person(3, "Bastiaan", 19));
        persons.add(new Person(4, "Sander", 17));
        persons.add(new Person(5, "Leonard", 14));
        persons.add(new Person(6, "Jiska", 13));
    }

    public static HomeScreen create(WidgetContext context) {
        return new HomeScreen(context);
    }

    public Widget build() {
        return Scroll.create(context)
            .child(Box.create(context)
                .child(
                    Text.create(context)
                        .text("ReactDroid")
                        .fontSizeSp(16)
                        .fontWeight(500)
                        .paddingDp(16)
                )
                .child(Button.create(context).text("Add person").onClick(view -> {
                    persons.add(new Person(persons.size() + 1, "Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                    refresh();
                }))
                .child(
                    persons.stream().map(person -> PersonItem.create(context).person(person).key(person.id))
                        .collect(Collectors.toList())
                )
                .child(Button.create(context).text("Add person").onClick(view -> {
                    persons.add(new Person(persons.size() + 1, "Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                    refresh();
                }))
                .child(
                    Text.create(context)
                        .text("Made by Bastiaan van der Plaat")
                        .textColorRes(R.color.secondary_text_color)
                        .paddingDp(16)
                )
            );
    }
}
