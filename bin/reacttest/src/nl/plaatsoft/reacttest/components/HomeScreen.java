/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import nl.plaatsoft.android.compat.MapList;
import nl.plaatsoft.android.reactdroid.Button;
import nl.plaatsoft.android.reactdroid.Column;
import nl.plaatsoft.android.reactdroid.Scroll;
import nl.plaatsoft.android.reactdroid.StatefulWidget;
import nl.plaatsoft.android.reactdroid.Text;
import nl.plaatsoft.android.reactdroid.Widget;
import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class HomeScreen extends StatefulWidget {
    protected MapList<Person> persons;

    public HomeScreen(WidgetContext context) {
        super(context);

        persons = new MapList<Person>(6);
        persons.add(new Person("Willem", 51));
        persons.add(new Person("Wietske", 47));
        persons.add(new Person("Bastiaan", 19));
        persons.add(new Person("Sander", 17));
        persons.add(new Person("Leonard", 14));
        persons.add(new Person("Jiska", 13));
    }

    public Widget build() {
        return new Scroll(c).useWindowInsets().child(new Column(c)
                .child(new Text(c).text("ReactTest").fontSizeSp(16).fontWeight(500).paddingDp(16))

                .child(persons.map(person
                    -> new PersonItem(c)
                        .person(person)
                        .onUpdate(updatedPerson -> {
                            persons = persons.map(p -> p.id() == updatedPerson.id() ? updatedPerson : p);
                            refresh();
                        })
                        .onDelete(id -> {
                            persons = persons.filter(p -> p.id() != id);
                            refresh();
                        })
                        .key(person.id())))

                .child(new Button(c).text("Add person").onClick(view -> {
                    persons.add(new Person("Person " + (persons.size() + 1), (int)(Math.random() * 100)));
                    refresh();
                }))
                .child(new Text(c)
                        .text("Made by Bastiaan van der Plaat")
                        .textColorRes(R.color.secondary_text_color)
                        .paddingDp(16)));
    }
}
