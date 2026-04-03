/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import android.content.Context;
import android.view.Gravity;

import nl.plaatsoft.android.react.Column;
import nl.plaatsoft.android.react.Component;
import nl.plaatsoft.android.react.ImageButton;
import nl.plaatsoft.android.react.LazyColumn;
import nl.plaatsoft.android.react.Modifier;
import nl.plaatsoft.android.react.Row;
import nl.plaatsoft.android.react.Text;
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
        persons = persons.stream().map(p -> p.id().equals(person.id()) ? person : p).collect(Collectors.toList());
        rebuild();
    }

    private void deletePerson(UUID id) {
        persons = persons.stream().filter(p -> !p.id().equals(id)).collect(Collectors.toList());
        rebuild();
    }

    @Override
    public void render() {
        new Column(() -> {
            new Row(() -> {
                new Text("ReactTest")
                    .modifier(Modifier.of().weight(1).fontSizeSp(20).fontWeight(500).paddingDp(0, 16).align(
                        Gravity.CENTER_VERTICAL));
                new ImageButton(R.drawable.ic_plus, () -> {
                    persons.add(new Person("Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                    rebuild();
                }).modifier(Modifier.of().size(56).backgroundRes(R.drawable.app_bar_icon_button_ripple));
            }).modifier(Modifier.of().fillMaxWidth().backgroundRes(R.color.primary_color).elevation(4));

            new CounterButton();

            new LazyColumn<>(persons, Person::id,
                person -> new PersonItem(person.id(), person, this::updatePerson, this::deletePerson))
                .modifier(Modifier.of().fillHeight());
        });
    }
}
