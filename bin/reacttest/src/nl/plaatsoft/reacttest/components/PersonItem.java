/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import java.util.UUID;

import nl.plaatsoft.android.reactdroid.Button;
import nl.plaatsoft.android.reactdroid.Column;
import nl.plaatsoft.android.reactdroid.Row;
import nl.plaatsoft.android.reactdroid.StatelessWidget;
import nl.plaatsoft.android.reactdroid.Text;
import nl.plaatsoft.android.reactdroid.Widget;
import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class PersonItem extends StatelessWidget {
    public static interface OnUpdate {
        void onUpdate(Person person);
    }
    public static interface OnDelete {
        void onDelete(UUID id);
    }

    protected Person person;
    protected OnUpdate onUpdate;
    protected OnDelete onDelete;

    public PersonItem(WidgetContext context) {
        super(context);
    }

    public PersonItem person(Person person) {
        this.person = person;
        return this;
    }

    public PersonItem onUpdate(OnUpdate onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    public PersonItem onDelete(OnDelete onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public Widget build() {
        return new Column(c)
            .paddingDp(8, 16)
            .child(!person.isDead() ? new Text(c).text("Hello " + person.name() + ", i'm " + person.age() + " old!")
                                    : new Text(c)
                                          .text("Oh no " + person.name() + " is dead!")
                                          .textColorRes(R.color.secondary_text_color))
            .child(!person.isDead()
                    ? new Row(c)
                          .child(new Button(c).text("+").onClick(view -> { onUpdate.onUpdate(person.ageInYears(1)); }))
                          .child(new Button(c).text("-").onClick(view -> { onUpdate.onUpdate(person.ageInYears(-1)); }))
                          .child(new Button(c).text("DEL").onClick(view -> { onDelete.onDelete(person.id()); }))
                    : null);
    }
}
