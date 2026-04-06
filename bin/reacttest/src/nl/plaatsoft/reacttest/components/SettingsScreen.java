/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.components;

import static nl.plaatsoft.android.react.Unit.*;
import static nl.plaatsoft.reacttest.components.Styles.*;

import android.app.Activity;
import android.content.Context;

import nl.plaatsoft.android.react.*;
import nl.plaatsoft.reacttest.R;

public class SettingsScreen extends Component {
    public SettingsScreen(Context context) {
        super(context);
    }

    @Override
    public void render() {
        new Column(() -> {
            new Row(() -> {
                new ImageButton(R.drawable.ic_arrow_left)
                    .onClick(() -> ((Activity)getContext()).finish())
                    .modifier(actionBarIconButton());
                new Text(R.string.settings).modifier(actionBarTitle());
            }).modifier(actionBar());

            new CounterButton();
        });
    }
}
