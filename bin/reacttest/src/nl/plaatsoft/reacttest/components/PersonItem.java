/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import nl.plaatsoft.android.reactdroid.Box;
import nl.plaatsoft.android.reactdroid.Button;
import nl.plaatsoft.android.reactdroid.StatefulWidget;
import nl.plaatsoft.android.reactdroid.Text;
import nl.plaatsoft.android.reactdroid.Widget;
import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class PersonItem extends StatefulWidget {
    protected Person person;

    protected PersonItem(WidgetContext context) {
        super(context);
    }

    public static PersonItem create(WidgetContext context) {
        return new PersonItem(context);
    }

    public PersonItem person(Person person) {
        this.person = person;
        return this;
    }

    public Widget build() {
        return Box.create(context)
                .child(!person.dead
                        ? Text.create(context).text("Hello " + person.name + ", i'm " + person.age + " old!")
                        : Text.create(context).text("Oh no " + person.name + " is dead!")
                                .textColorRes(R.color.secondary_text_color))
                .child(
                        !person.dead
                                ? Box.create(context).orientation(Box.HORIZONTAL)
                                        .child(Button.create(context).text("+").onClick(view -> {
                                            if (person.age < 100)
                                                person.age++;
                                            if (person.age == 100)
                                                person.dead = true;
                                            refresh();
                                        }))
                                        .child(Button.create(context).text("-").onClick(view -> {
                                            if (person.age > 0)
                                                person.age--;
                                            refresh();
                                        }))
                                : null)
                .paddingDp(8, 16);
    }
}
