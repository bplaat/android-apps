/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import static nl.plaatsoft.android.react.Unit.*;

import nl.plaatsoft.android.react.*;

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
            new Text("Count: " + count)
                .modifier(Modifier.of().fontSize(sp(16)).fontWeight(Modifier.FontWeight.MEDIUM).weight(1));
            new Button("-").onClick(() -> {
                count = Math.max(0, count - 1);
                rebuild();
            });
            new Button("+").onClick(() -> {
                count++;
                rebuild();
            });
        }).modifier(Modifier.of().padding(dp(8), dp(16)));
    }
}
