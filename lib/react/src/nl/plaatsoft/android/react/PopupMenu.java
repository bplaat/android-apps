/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

public class PopupMenu {
    private record Item(String title, int titleRes, Runnable onClick) {}

    private final android.widget.PopupMenu inner;
    private final List<Item> items = new ArrayList<>();

    public PopupMenu(Context context, View anchor) {
        this(context, anchor, Gravity.TOP | Gravity.END);
    }

    public PopupMenu(Context context, View anchor, int gravity) {
        inner = new android.widget.PopupMenu(context, anchor, gravity);
    }

    public PopupMenu item(String title, Runnable onClick) {
        items.add(new Item(title, 0, onClick));
        return this;
    }

    public PopupMenu item(int titleRes, Runnable onClick) {
        items.add(new Item(null, titleRes, onClick));
        return this;
    }

    public void show() {
        inner.getMenu().clear();
        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (item.title() != null) {
                inner.getMenu().add(0, i, 0, item.title());
            } else {
                inner.getMenu().add(0, i, 0, item.titleRes());
            }
        }
        inner.setOnMenuItemClickListener(menuItem -> {
            items.get(menuItem.getItemId()).onClick().run();
            return true;
        });
        inner.show();
    }
}
