/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import static nl.plaatsoft.android.react.Unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.view.Gravity;

import nl.plaatsoft.android.react.*;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class HomeScreen extends Component {
    private List<Person> persons;

    public HomeScreen(Context context) {
        super(context);

        persons = new ArrayList<>(6);
        persons.add(new Person("Willem", 51));
        persons.add(new Person("Wietske", 47));
        persons.add(new Person("Bastiaan", 19));
        persons.add(new Person("Sander", 17));
        persons.add(new Person("Leonard", 14));
        persons.add(new Person("Jiska", 13));
    }

    private void updatePerson(Person person) {
        persons.replaceAll(p -> p.id().equals(person.id()) ? person : p);
        rebuild();
    }

    private void deletePerson(UUID id) {
        persons.removeIf(p -> p.id().equals(id));
        rebuild();
    }

    @Override
    public void render() {
        new Column(() -> {
            new Row(() -> {
                new Text("ReactTest").modifier(actionBarTitle());
                new ImageButton(R.drawable.ic_plus)
                    .onClick(() -> {
                        persons.add(new Person("Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                        rebuild();
                    })
                    .modifier(actionBarIconButton());
            }).modifier(actionBar());

            new CounterButton();

            if (persons.isEmpty()) {
                new Box(() -> {
                    new Text(R.string.persons_empty)
                        .modifier(Modifier.of().align(Gravity.CENTER).textColor(R.color.secondary_text_color));
                }).modifier(Modifier.of().width(matchParent()).weight(1));
            } else {
                new LazyColumn<>(
                    persons, Person::id, person -> new PersonItem(person, this::updatePerson, this::deletePerson))
                    .modifier(Modifier.of().width(matchParent()).weight(1));
            }
        });
    }

    private static Modifier actionBar() {
        return Modifier.of().width(matchParent()).background(R.color.primary_color).elevation(dp(4));
    }
    private static Modifier actionBarTitle() {
        return Modifier.of()
            .weight(1)
            .fontSize(sp(20))
            .fontWeight(Modifier.FontWeight.MEDIUM)
            .paddingX(dp(16))
            .align(Gravity.CENTER_VERTICAL);
    }
    private static Modifier actionBarIconButton() {
        return Modifier.of().size(dp(56)).background(R.drawable.app_bar_icon_button_ripple);
    }
}
