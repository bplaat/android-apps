/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import org.jspecify.annotations.Nullable;

public class LazyColumn<T> {
    private static final int TAG_HEADER = 0x68656164; // 'head'

    private static class ItemAdapter<T> extends BaseAdapter {
        private final Context context;
        private List<T> items;
        private final @Nullable Function<T, ?> keyExtractor;
        private Consumer<T> renderer;

        ItemAdapter(Context context, List<T> items, @Nullable Function<T, ?> keyExtractor, Consumer<T> renderer) {
            this.context = context;
            this.items = items;
            this.keyExtractor = keyExtractor;
            this.renderer = renderer;
        }

        void updateItems(List<T> newItems, Consumer<T> newRenderer) {
            items = newItems;
            renderer = newRenderer;
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
            if (key == null)
                return position;
            if (key instanceof Long l)
                return l;
            if (key instanceof Number n)
                return n.longValue();
            if (key instanceof UUID u)
                return u.getLeastSignificantBits() ^ u.getMostSignificantBits();
            return key.hashCode();
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

    private final android.widget.ListView ref;

    public LazyColumn(List<T> items, Consumer<T> renderer) {
        this(items, (Runnable)null, (Function<T, ?>)null, renderer);
    }

    public LazyColumn(List<T> items, @Nullable Runnable header, Consumer<T> renderer) {
        this(items, header, (Function<T, ?>)null, renderer);
    }

    public LazyColumn(List<T> items, Function<T, ?> keyExtractor, Consumer<T> renderer) {
        this(items, (Runnable)null, keyExtractor, renderer);
    }

    public LazyColumn(List<T> items, Function<T, ?> keyExtractor, @Nullable Runnable header, Consumer<T> renderer) {
        this(items, header, keyExtractor, renderer);
    }

    private LazyColumn(
        List<T> items, @Nullable Runnable header, @Nullable Function<T, ?> keyExtractor, Consumer<T> renderer) {
        BuildContext c = BuildContext.current();
        var lv = c.slot(android.widget.ListView.class, () -> new android.widget.ListView(c.getContext()));
        ref = lv;
        if (lv.getAdapter() == null) {
            if (header != null) {
                var hc = renderBlock(c.getContext(), null, header);
                lv.setTag(TAG_HEADER, hc);
                lv.addHeaderView(hc, null, false);
            }
            lv.addFooterView(new android.view.View(c.getContext()), null, false);
            lv.setFooterDividersEnabled(false);
            lv.setAdapter(new ItemAdapter<>(c.getContext(), items, keyExtractor, renderer));
        } else {
            var rawAdapter = lv.getAdapter();
            @SuppressWarnings("unchecked")
            var adapter = (ItemAdapter<T>)(rawAdapter instanceof android.widget.HeaderViewListAdapter
                    ? ((android.widget.HeaderViewListAdapter)rawAdapter).getWrappedAdapter()
                    : rawAdapter);
            adapter.updateItems(items, renderer);
            if (header != null) {
                renderBlock(c.getContext(), (FrameLayout)lv.getTag(TAG_HEADER), header);
            }
        }
    }

    private static FrameLayout renderBlock(Context ctx, @Nullable FrameLayout existing, Runnable content) {
        var container = existing != null ? existing : new FrameLayout(ctx);
        var bc = new BuildContext(ctx, container);
        BuildContext.push(bc);
        try {
            content.run();
        } finally {
            bc.cleanup();
            BuildContext.pop();
        }
        return container;
    }

    public LazyColumn<T> modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        return this;
    }
}
