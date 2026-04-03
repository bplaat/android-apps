/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.function.Supplier;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

/// A slot-based view allocator that tracks position within a ViewGroup.
/// Passed to widget constructors so they can get/create their View at the
/// current position. Children advance the index; cleanup() prunes orphaned views.
///
/// Context flows implicitly via a per-thread stack: push() before entering a
/// render scope, pop() (in a finally block) when leaving. All widget constructors
/// call current() to get the active context rather than accepting it as a parameter.
public class BuildContext {
    // Thread-local stack so context flows implicitly through the render call tree.
    // Android renders UI on the main thread only, so this is safe.
    private static final java.util.Deque<BuildContext> stack = new java.util.ArrayDeque<>();

    public static void push(BuildContext c) {
        stack.push(c);
    }

    public static void pop() {
        stack.pop();
    }

    /// Returns the active BuildContext for the current render scope.
    /// Throws if called outside a render scope (i.e. no push() is active).
    public static BuildContext current() {
        BuildContext c = stack.peek();
        if (c == null)
            throw new IllegalStateException("No active BuildContext");
        return c;
    }

    private final Context context;
    private final ViewGroup parent;
    private int index = 0;

    // Tag ID for storing a slot key on a View. Using a stable magic number since
    // the react library has no access to an R.id resource namespace.
    static final int TAG_SLOT_KEY = 0x636f6d70; // 'comp'

    public BuildContext(Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
    }

    public Context getContext() {
        return context;
    }

    /// Get the existing view at the current slot if its class matches, otherwise
    /// create a new one via the supplier and insert it at the current position.
    @SuppressWarnings("unchecked")
    public <V extends View> V slot(Class<V> type, Supplier<V> create) {
        @Nullable View existing = parent.getChildAt(index);
        V view;
        if (existing != null && existing.getClass() == type) {
            view = (V)existing;
        } else {
            view = create.get();
            if (existing != null) {
                parent.removeViewAt(index);
                parent.addView(view, index);
            } else {
                parent.addView(view);
            }
        }
        index++;
        return view;
    }

    /// Keyed variant: matches an existing view by key AND class regardless of position.
    /// Scans forward from the current index so already-consumed slots are never revisited.
    /// If a matching view is found ahead, it is moved to the current slot; if none is
    /// found a new view is created and tagged with the key. Views whose keys are never
    /// matched in a render pass are pruned by cleanup().
    @SuppressWarnings("unchecked")
    public <V extends View> V slot(Object key, Class<V> type, Supplier<V> create) {
        // Fast path: current slot already has the right key + type.
        View existing = parent.getChildAt(index);
        if (existing != null && existing.getClass() == type && key.equals(existing.getTag(TAG_SLOT_KEY))) {
            index++;
            return (V)existing;
        }
        // Search forward for a view with a matching key + type.
        for (int i = index + 1; i < parent.getChildCount(); i++) {
            View candidate = parent.getChildAt(i);
            if (candidate.getClass() == type && key.equals(candidate.getTag(TAG_SLOT_KEY))) {
                // Move it to the current position; the views between index and i shift right.
                parent.removeViewAt(i);
                parent.addView(candidate, index);
                index++;
                return (V)candidate;
            }
        }
        // Not found: create a new tagged view and insert without displacing the current slot.
        V view = create.get();
        view.setTag(TAG_SLOT_KEY, key);
        parent.addView(view, index);
        index++;
        return view;
    }

    /// Create a new BuildContext scoped to a child ViewGroup, resetting the index.
    public BuildContext scope(ViewGroup child) {
        return new BuildContext(context, child);
    }

    /// Remove any views beyond the current index (orphaned from a previous render).
    public void cleanup() {
        while (parent.getChildCount() > index) {
            parent.removeViewAt(index);
        }
    }

    /// Returns the view at the current slot index without advancing, or null if the slot is empty.
    public @Nullable View peekSlot() {
        return parent.getChildAt(index);
    }

    /// Advance the slot index by one. Used when the caller takes ownership of the current slot
    /// (e.g. after peekSlot() confirms the existing view can be reused as-is).
    public void advanceSlot() {
        index++;
    }
}
