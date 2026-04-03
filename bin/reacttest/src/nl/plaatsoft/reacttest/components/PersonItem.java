/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import java.util.UUID;
import java.util.function.Consumer;

import nl.plaatsoft.android.react.Button;
import nl.plaatsoft.android.react.Column;
import nl.plaatsoft.android.react.Modifier;
import nl.plaatsoft.android.react.Row;
import nl.plaatsoft.android.react.Text;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

/// A stateless widget rendered as a constructor call -- no base class needed.
public class PersonItem {
    public PersonItem(Object key, Person person, Consumer<Person> onUpdate, Consumer<UUID> onDelete) {
        new Column(key, () -> {
            if (!person.isDead()) {
                new Text("Hello " + person.name() + ", i'm " + person.age() + " old!");
                new Row(() -> {
                    new Button("+", () -> onUpdate.accept(person.withAgeIncrement(1)));
                    new Button("-", () -> onUpdate.accept(person.withAgeIncrement(-1)));
                    new Button("DEL", () -> onDelete.accept(person.id()));
                });
            } else {
                new Text("Oh no, " + person.name() + " is dead!")
                    .modifier(Modifier.of().textColorRes(R.color.secondary_text_color));
            }
        }).modifier(Modifier.of().paddingDp(8, 16));
    }
}
