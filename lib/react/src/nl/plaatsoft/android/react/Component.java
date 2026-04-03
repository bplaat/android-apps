/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.jspecify.annotations.Nullable;

/// Base class for stateful UI components. Extends FrameLayout so it can be
/// passed directly to Activity.setContentView(). Call rebuild() from event
/// handlers to re-render after state changes.
///
/// Two constructor forms:
///   Component(Context) -- root usage: Activity.setContentView(new MyScreen(this))
///   Component()        -- nested usage: new MyWidget() inside a parent render()
///
/// For nested use, the no-arg constructor reads BuildContext.current() and self-slots:
/// on first render 'this' is inserted; on subsequent renders the existing instance
/// (with its state intact) is reused and 'this' is discarded. Initial render is
/// deferred to onAttachedToWindow() so subclass field initializers run first.
///
/// Lifecycle (override any you need, all have empty defaults):
///   render()    -- called every time the component re-renders
///   onMount()   -- called once after the first render
///   onUpdate()  -- called after every subsequent rebuild()
///   onUnmount() -- called when the component is removed from the window
public abstract class Component extends FrameLayout {
    private boolean initialized = false;
    private boolean mounted = false;
    // Non-null only when constructed via no-arg constructor; points to the actual
    // slotted view (may differ from 'this' on re-renders of nested components).
    private @Nullable View slottedView = null;

    public Component(Context context) {
        super(context);
    }

    // Nested-component constructor: reads BuildContext.current() and self-slots.
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component() {
        super(BuildContext.current().getContext());
        BuildContext c = BuildContext.current();
        // slot() returns the existing view if its class matches, otherwise
        // adds 'this' and returns it. State lives on the returned instance.
        slottedView = c.slot((Class)getClass(), () -> this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!initialized) {
            initialized = true;
            rebuild();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mounted) {
            mounted = false;
            initialized = false;
            onUnmount();
        }
    }

    protected void rebuild() {
        var bc = new BuildContext(getContext(), this);
        BuildContext.push(bc);
        try {
            render();
        } finally {
            bc.cleanup();
            BuildContext.pop();
        }
        if (!mounted) {
            mounted = true;
            onMount();
        } else {
            onUpdate();
        }
    }

    /// Apply layout and visual properties. When used as a nested widget the
    /// modifier is applied to the actual slotted view, which may be a different
    /// instance than 'this' (the caller) on re-renders.
    public Component modifier(Modifier modifier) {
        var target = slottedView != null ? slottedView : this;
        modifier.applyTo(target);
        modifier.applyLayoutTo(target);
        return this;
    }

    public abstract void render();

    /// Called once after the first render. Use for post-mount initialization
    /// (e.g. starting timers, fetching data). May call rebuild().
    protected void onMount() {}

    /// Called after every re-render triggered by rebuild(). Use for
    /// side-effects that depend on updated state.
    protected void onUpdate() {}

    /// Called when the component is removed from the window. Use for cleanup
    /// (e.g. cancelling timers, releasing resources).
    protected void onUnmount() {}
}
