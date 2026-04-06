/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import static nl.plaatsoft.android.react.Unit.*;

import java.util.UUID;
import java.util.function.Consumer;

import nl.plaatsoft.android.react.*;
import nl.plaatsoft.reacttest.R;
import nl.plaatsoft.reacttest.models.Person;

public class PersonItem {
    public PersonItem(Person person, Consumer<Person> onUpdate, Consumer<UUID> onDelete) {
        new Column(() -> {
            if (!person.isDead()) {
                new Text("Hello " + person.name() + ", i'm " + person.age() + " old!");
                new Row(() -> {
                    new Button("+").onClick(() -> onUpdate.accept(person.withAgeIncrement(1)));
                    new Button("-").onClick(() -> onUpdate.accept(person.withAgeIncrement(-1)));
                    new Spacer().modifier(Modifier.of().weight(1));
                    new ImageButton(R.drawable.ic_dots_vertical)
                        .onClick(view
                            -> new PopupMenu(view.getContext(), view)
                                .item(R.string.delete, () -> onDelete.accept(person.id()))
                                .show())
                        .modifier(Modifier.of().background(R.drawable.app_bar_icon_button_ripple));
                });
            } else {
                new Text("Oh no, " + person.name() + " is dead!")
                    .modifier(Modifier.of().textColor(R.color.secondary_text_color));
            }
        }).modifier(Modifier.of().padding(dp(8), dp(16)));
    }
}
