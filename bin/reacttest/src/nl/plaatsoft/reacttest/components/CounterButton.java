/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import nl.plaatsoft.android.react.Button;
import nl.plaatsoft.android.react.Component;
import nl.plaatsoft.android.react.Modifier;
import nl.plaatsoft.android.react.Row;
import nl.plaatsoft.android.react.Text;

public class CounterButton extends Component {
    private int count = 0;

    public CounterButton() {
        super();
    }

    @Override
    protected void onMount() {
        count = 5;
        rebuild();
    }

    @Override
    public void render() {
        new Row(() -> {
            new Text("Count: " + count).modifier(Modifier.of().fontSizeSp(16).paddingDp(8, 12).weight(1));
            new Button("-", () -> {
                count = Math.max(0, count - 1);
                rebuild();
            });
            new Button("+", () -> {
                count++;
                rebuild();
            });
        }).modifier(Modifier.of().fillMaxWidth());
    }
}
