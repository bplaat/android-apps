/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import org.jspecify.annotations.Nullable;

public class LazyColumn<T> {
    private static class ItemAdapter<T> extends BaseAdapter {
        private final Context context;
        private List<T> items;
        private final @Nullable Function<T, ?> keyExtractor;
        private final Consumer<T> renderer;

        ItemAdapter(Context context, List<T> items, @Nullable Function<T, ?> keyExtractor, Consumer<T> renderer) {
            this.context = context;
            this.items = items;
            this.keyExtractor = keyExtractor;
            this.renderer = renderer;
        }

        void updateItems(List<T> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public boolean hasStableIds() {
            return keyExtractor != null;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public T getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (keyExtractor == null)
                return position;
            var key = keyExtractor.apply(items.get(position));
            return key != null ? key.hashCode() : position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout container;
            if (convertView instanceof FrameLayout) {
                container = (FrameLayout)convertView;
            } else {
                container = new FrameLayout(context);
            }
            var itemC = new BuildContext(context, container);
            BuildContext.push(itemC);
            try {
                renderer.accept(items.get(position));
            } finally {
                itemC.cleanup();
                BuildContext.pop();
            }
            return container;
        }
    }

    private final android.widget.ListView lv_ref;

    /// Position-based adapter (no stable IDs).
    public LazyColumn(List<T> items, Consumer<T> renderer) {
        this(items, null, renderer);
    }

    /// Keyed adapter: keyExtractor provides a stable ID per item so ListView
    /// can match recycled views by identity rather than position.
    public LazyColumn(List<T> items, Function<T, ?> keyExtractor, Consumer<T> renderer) {
        BuildContext c = BuildContext.current();
        var lv = c.slot(android.widget.ListView.class, () -> new android.widget.ListView(c.getContext()));
        lv_ref = lv;
        if (lv.getAdapter() == null) {
            lv.setAdapter(new ItemAdapter<>(c.getContext(), items, keyExtractor, renderer));
        } else {
            @SuppressWarnings("unchecked") var adapter = (ItemAdapter<T>)lv.getAdapter();
            adapter.updateItems(items);
        }
    }

    public LazyColumn<T> modifier(Modifier modifier) {
        modifier.applyTo(lv_ref);
        modifier.applyLayoutTo(lv_ref);
        return this;
    }
}
