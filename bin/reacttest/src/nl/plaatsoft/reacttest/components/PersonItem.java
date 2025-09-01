/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import nl.plaatsoft.android.reactdroid.Button;
import nl.plaatsoft.android.reactdroid.Column;
import nl.plaatsoft.android.reactdroid.Row;
import nl.plaatsoft.android.reactdroid.StatefulWidget;
import nl.plaatsoft.android.reactdroid.Text;
import nl.plaatsoft.android.reactdroid.Widget;
import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class PersonItem extends StatefulWidget {
    protected Person person;

    public PersonItem(WidgetContext context) {
        super(context);
    }

    public PersonItem person(Person person) {
        this.person = person;
        return this;
    }

    public Widget build() {
        return new Column(c)
            .paddingDp(8, 16)
            .child(!person.dead
                    ? new Text(c).text("Hello " + person.name + ", i'm " + person.age + " old!")
                    : new Text(c).text("Oh no " + person.name + " is dead!").textColorRes(R.color.secondary_text_color))
            .child(!person.dead ? new Row(c)
                                      .child(new Button(c).text("+").onClick(view -> {
                                          if (person.age < 100)
                                              person.age++;
                                          if (person.age == 100)
                                              person.dead = true;
                                          refresh();
                                      }))
                                      .child(new Button(c).text("-").onClick(view -> {
                                          if (person.age > 0)
                                              person.age--;
                                          refresh();
                                      }))
                                : null);
    }
}
