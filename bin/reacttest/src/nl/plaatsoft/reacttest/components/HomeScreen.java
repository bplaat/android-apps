/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import nl.plaatsoft.android.compat.MapList;
import nl.plaatsoft.android.reactdroid.Box;
import nl.plaatsoft.android.reactdroid.Button;
import nl.plaatsoft.android.reactdroid.Scroll;
import nl.plaatsoft.android.reactdroid.StatefulWidget;
import nl.plaatsoft.android.reactdroid.Text;
import nl.plaatsoft.android.reactdroid.Widget;
import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class HomeScreen extends StatefulWidget {
    protected MapList<Person> persons;

    protected HomeScreen(WidgetContext context) {
        super(context);

        persons = new MapList<Person>(6);
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
                                        .text("ReactTest")
                                        .fontSizeSp(16)
                                        .fontWeight(500)
                                        .paddingDp(16))
                        .child(Button.create(context).text("Add person").onClick(view -> {
                            persons.add(new Person(persons.size() + 1, "Person " + (persons.size() + 1),
                                    (int) (Math.random() * 100)));
                            refresh();
                        }))

                        .child(persons.map(person -> PersonItem.create(context).person(person).key(person.id)))

                        .child(Button.create(context).text("Add person").onClick(view -> {
                            persons.add(new Person(persons.size() + 1, "Person " + (persons.size() + 1),
                                    (int) (Math.random() * 100)));
                            refresh();
                        }))
                        .child(
                                Text.create(context)
                                        .text("Made by Bastiaan van der Plaat")
                                        .textColorRes(R.color.secondary_text_color)
                                        .paddingDp(16)));
    }
}
